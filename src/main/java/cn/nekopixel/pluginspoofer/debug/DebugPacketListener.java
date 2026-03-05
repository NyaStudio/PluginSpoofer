package cn.nekopixel.pluginspoofer.debug;

import cn.nekopixel.pluginspoofer.config.ConfigManager;
import cn.nekopixel.pluginspoofer.utils.UnknownCommandRewriteTracker;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTabComplete;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Locale;

public class DebugPacketListener extends PacketListenerAbstract {

    private final Plugin plugin;
    private final ConfigManager config;

    public DebugPacketListener(Plugin plugin, ConfigManager config) {
        super(PacketListenerPriority.HIGHEST);
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CHAT_COMMAND) {
            WrapperPlayClientChatCommand chatCommand = new WrapperPlayClientChatCommand(event);
            String command = extractCommandLabel(chatCommand.getCommand());
            
            for (String blocked : config.getBlockedCommands()) {
                if (matchesBlockedCommand(command, blocked)) {
                    if (config.isDebugEnabled()) {
                        plugin.getLogger().info("[Debug] Blocked command detected, pass to CommandListener: /" + chatCommand.getCommand());
                    }
                    return;
                }
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.CHAT_MESSAGE) {
            WrapperPlayClientChatMessage chatMessage = new WrapperPlayClientChatMessage(event);
            String rawMessage = chatMessage.getMessage();
            if (rawMessage == null) {
                return;
            }

            String message = rawMessage.toLowerCase(Locale.ROOT);
            
            if (message.startsWith("/")) {
                String command = extractCommandLabel(message);
                for (String blocked : config.getBlockedCommands()) {
                    if (matchesBlockedCommand(command, blocked)) {
                        if (config.isDebugEnabled()) {
                            plugin.getLogger().info("[Debug] Blocked command detected, pass to CommandListener: " + chatMessage.getMessage());
                        }
                        return;
                    }
                }
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE) {
            WrapperPlayClientTabComplete tabComplete = new WrapperPlayClientTabComplete(event);
            String text = tabComplete.getText();
            if (text == null) {
                text = "";
            }
            String commandToken = extractCommandLabel(text);
            String normalizedText = text.trim().toLowerCase(Locale.ROOT);
            
            if (normalizedText.equals("/") && config.shouldBlockSlashCompletion()) {
                event.setCancelled(true);
                if (config.isDebugEnabled()) {
                    plugin.getLogger().info("[Debug] Caught Tab Request: " + tabComplete.getText());
                }
                return;
            }
            
            for (String blocked : config.getBlockedCommands()) {
                if (isBlockedTabInput(commandToken, blocked)) {
                    event.setCancelled(true);
                    if (config.isDebugEnabled()) {
                        plugin.getLogger().info("[Debug] Caught Tab Request: " + tabComplete.getText());
                    }
                    return;
                }
            }
            
            if (config.shouldBlockNonMinecraftNamespaces() && commandToken.contains(":")) {
                String[] parts = commandToken.split(":", 2);
                if (parts.length > 0) {
                    String namespace = parts[0];
                    if (!namespace.equals("minecraft")) {
                        event.setCancelled(true);
                        if (config.isDebugEnabled()) {
                            plugin.getLogger().info("[Debug] Caught Non-minecraft namespace: " + tabComplete.getText());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.SYSTEM_CHAT_MESSAGE) {
            return;
        }

        Object packetPlayer = event.getPlayer();
        if (!(packetPlayer instanceof Player player)) {
            return;
        }

        WrapperPlayServerSystemChatMessage systemChat = new WrapperPlayServerSystemChatMessage(event);
        String messageJson = systemChat.getMessageJson();
        if (messageJson == null || messageJson.isEmpty()) {
            return;
        }

        String rewrittenJson = UnknownCommandRewriteTracker.replaceIfTracked(player.getUniqueId(), messageJson);
        if (!messageJson.equals(rewrittenJson)) {
            systemChat.setMessageJson(rewrittenJson);
            if (config.isDebugEnabled()) {
                plugin.getLogger().info("[Debug] Restore original blocked command label in unknown response for " + player.getName());
            }
        }
    }

    private String extractCommandLabel(String rawInput) {
        if (rawInput == null) {
            return "";
        }

        String trimmed = rawInput.trim();
        if (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }

        int spaceIndex = trimmed.indexOf(' ');
        if (spaceIndex >= 0) {
            trimmed = trimmed.substring(0, spaceIndex);
        }

        return trimmed.toLowerCase(Locale.ROOT);
    }

    private String normalizeBlockedCommand(String blockedCommand) {
        if (blockedCommand == null) {
            return "";
        }

        String normalized = blockedCommand.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        return normalized;
    }

    private boolean matchesBlockedCommand(String command, String blockedCommand) {
        String normalizedBlocked = normalizeBlockedCommand(blockedCommand);
        if (command.isEmpty() || normalizedBlocked.isEmpty()) {
            return false;
        }

        if (command.equals(normalizedBlocked)) {
            return true;
        }

        if (!normalizedBlocked.contains(":")) {
            int namespaceIndex = command.indexOf(':');
            if (namespaceIndex >= 0 && namespaceIndex + 1 < command.length()) {
                String withoutNamespace = command.substring(namespaceIndex + 1);
                return withoutNamespace.equals(normalizedBlocked);
            }
        }

        return false;
    }

    private boolean isBlockedTabInput(String commandToken, String blockedCommand) {
        String normalizedBlocked = normalizeBlockedCommand(blockedCommand);
        if (commandToken.isEmpty() || normalizedBlocked.isEmpty()) {
            return false;
        }

        if (normalizedBlocked.startsWith(commandToken)) {
            return true;
        }

        if (!normalizedBlocked.contains(":")) {
            int namespaceIndex = commandToken.indexOf(':');
            if (namespaceIndex >= 0 && namespaceIndex + 1 < commandToken.length()) {
                String withoutNamespace = commandToken.substring(namespaceIndex + 1);
                return normalizedBlocked.startsWith(withoutNamespace);
            }
        }

        return false;
    }

}
