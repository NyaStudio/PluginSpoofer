package cn.nekopixel.pluginspoofer.debug;

import cn.nekopixel.pluginspoofer.config.ConfigManager;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTabComplete;
import org.bukkit.plugin.Plugin;

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
            String command = chatCommand.getCommand().toLowerCase();
            
            for (String blocked : config.getBlockedCommands()) {
                String blockedLower = blocked.toLowerCase();
                if (command.startsWith(blockedLower) || command.contains(":" + blockedLower)) {
                    event.setCancelled(true);
                    if (config.isDebugEnabled()) {
                        plugin.getLogger().info("拦截了命令: /" + chatCommand.getCommand());
                    }
                    return;
                }
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.CHAT_MESSAGE) {
            WrapperPlayClientChatMessage chatMessage = new WrapperPlayClientChatMessage(event);
            String message = chatMessage.getMessage().toLowerCase();
            
            if (message.startsWith("/")) {
                String command = message.substring(1).split(" ")[0];
                for (String blocked : config.getBlockedCommands()) {
                    String blockedLower = blocked.toLowerCase();
                    if (command.equals(blockedLower) || command.contains(":" + blockedLower)) {
                        event.setCancelled(true);
                        if (config.isDebugEnabled()) {
                            plugin.getLogger().info("拦截了命令: " + chatMessage.getMessage());
                        }
                        return;
                    }
                }
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE) {
            WrapperPlayClientTabComplete tabComplete = new WrapperPlayClientTabComplete(event);
            String text = tabComplete.getText().toLowerCase();
            
            if ((text.equals("/") || text.trim().equals("/")) && config.shouldBlockSlashCompletion()) {
                event.setCancelled(true);
                if (config.isDebugEnabled()) {
                    plugin.getLogger().info("拦截了 Tab 补全请求: " + tabComplete.getText());
                }
                return;
            }
            
            for (String blocked : config.getBlockedCommands()) {
                String blockedLower = blocked.toLowerCase();
                if (text.startsWith("/" + blockedLower) || text.contains(":" + blockedLower)) {
                    event.setCancelled(true);
                    if (config.isDebugEnabled()) {
                        plugin.getLogger().info("拦截了 Tab 补全请求: " + tabComplete.getText());
                    }
                    return;
                }
            }
            
            if (config.shouldBlockNonMinecraftNamespaces() && text.contains(":")) {
                String[] parts = text.split(":");
                if (parts.length > 0) {
                    String namespace = parts[0].replace("/", "");
                    if (!namespace.equals("minecraft")) {
                        event.setCancelled(true);
                        if (config.isDebugEnabled()) {
                            plugin.getLogger().info("拦截了非 minecraft 命名空间: " + tabComplete.getText());
                        }
                    }
                }
            }
        }
    }
}
