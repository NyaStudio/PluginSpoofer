package cn.nekopixel.pluginspoofer.listener;

import cn.nekopixel.pluginspoofer.config.ConfigManager;
import cn.nekopixel.pluginspoofer.utils.PluginListSender;
import cn.nekopixel.pluginspoofer.utils.ModernMessageBuilder;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Locale;

public class CommandListener implements Listener {
    private final ConfigManager config;
    private final PluginListSender pluginListSender;
    private final Plugin plugin;
    private final BukkitAudiences adventure;
    
    public CommandListener(Plugin plugin, ConfigManager config, BukkitAudiences adventure) {
        this.plugin = plugin;
        this.config = config;
        this.adventure = adventure;
        this.pluginListSender = new PluginListSender(config, adventure);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String command = extractCommandLabel(event.getMessage());

        for (String blocked : config.getBlockedCommands()) {
            if (matchesBlockedCommand(command, blocked)) {
                
                if (config.isDebugEnabled()) {
                    plugin.getLogger().info("[Debug] Caught Command: " + event.getMessage());
                }
                
                event.setCancelled(true);
                
                if (isPluginListCommand(command) && config.isCustomPluginListEnabled()) {

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        pluginListSender.sendCustomPluginList(event.getPlayer());
                    }, 1L);
                    
                } else {
                    if (config.isDebugEnabled()) {
                        plugin.getLogger().info("[Debug] Send unknown command: " + event.getMessage());
                    }
                    
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (config.isModernServerEnabled()) {
                            Component modernMessage = ModernMessageBuilder.createModernUnknownCommandMessage(event.getMessage());
                            sendAdventureMessage(event.getPlayer(), modernMessage);
                        } else {
                            Component legacyMessage = ModernMessageBuilder.createLegacyUnknownCommandMessage();
                            sendAdventureMessage(event.getPlayer(), legacyMessage);
                        }
                    }, 1L);
                }
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandSend(PlayerCommandSendEvent event) {
        if (config.shouldBlockNonMinecraftNamespaces()) {
            event.getCommands().removeIf(cmd -> cmd.contains(":"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTabComplete(TabCompleteEvent event) {
        String buffer = event.getBuffer() == null ? "" : event.getBuffer();
        String commandToken = extractCommandLabel(buffer);
        String normalizedBuffer = buffer.trim().toLowerCase(Locale.ROOT);

        if (normalizedBuffer.equals("/") && config.shouldBlockSlashCompletion()) {
            event.setCompletions(new ArrayList<>());
            return;
        }

        for (String blocked : config.getBlockedCommands()) {
            if (isBlockedTabInput(commandToken, blocked)) {
                event.setCancelled(true);
                event.setCompletions(new ArrayList<>());
                return;
            }
        }
        
        if (config.shouldBlockNonMinecraftNamespaces() && commandToken.contains(":") && !event.isCancelled()) {
            String[] parts = commandToken.split(":", 2);
            if (parts.length > 0) {
                String namespace = parts[0];
                if (!namespace.equals("minecraft")) {
                    event.setCancelled(true);
                    event.setCompletions(new ArrayList<>());
                }
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

    private boolean isPluginListCommand(String command) {
        if (command.equals("pl") || command.equals("plugins") ||
            command.equals("bukkit:pl") || command.equals("bukkit:plugins")) {
            return true;
        }

        int namespaceIndex = command.indexOf(':');
        if (namespaceIndex >= 0 && namespaceIndex + 1 < command.length()) {
            String withoutNamespace = command.substring(namespaceIndex + 1);
            return withoutNamespace.equals("pl") || withoutNamespace.equals("plugins");
        }

        return false;
    }
    
    private void sendAdventureMessage(Player player, Component message) {
        try {
            if (adventure != null) {
                adventure.player(player).sendMessage(message);
            } else {
                try {
                    player.getClass().getMethod("sendMessage", Component.class).invoke(player, message);
                } catch (Exception paperEx) {
                    String legacyText = LegacyComponentSerializer.legacySection().serialize(message);
                    player.sendMessage(legacyText);
                }
            }
        } catch (Exception e) {
            if (config.isDebugEnabled()) {
                plugin.getLogger().warning("[Debug] Send Message Failed: " + e.getMessage());
            }
            try {
                String legacyText = LegacyComponentSerializer.legacySection().serialize(message);
                player.sendMessage(legacyText);
            } catch (Exception e2) {
                player.sendMessage("Unknown command. Type \"/help\" for help.");
            }
        }
    }
}
