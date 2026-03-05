package cn.nekopixel.pluginspoofer.listener;

import cn.nekopixel.pluginspoofer.config.ConfigManager;
import cn.nekopixel.pluginspoofer.utils.PluginListSender;
import cn.nekopixel.pluginspoofer.utils.UnknownCommandRewriteTracker;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
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
    
    public CommandListener(Plugin plugin, ConfigManager config, BukkitAudiences adventure) {
        this.plugin = plugin;
        this.config = config;
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

                if (isPluginListCommand(command) && config.isCustomPluginListEnabled()) {
                    event.setCancelled(true);

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        pluginListSender.sendCustomPluginList(event.getPlayer());
                    }, 1L);
                    
                } else {
                    rewriteBlockedCommandToUnknown(event, command);
                }
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandSend(PlayerCommandSendEvent event) {
        event.getCommands().removeIf(this::shouldHideFromCommandTree);
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

    private boolean shouldHideFromCommandTree(String rawCommand) {
        String command = extractCommandLabel(rawCommand);
        if (command.isEmpty()) {
            return false;
        }

        if (isInternalPluginCommand(command)) {
            return true;
        }

        if (config.isCustomPluginListEnabled() && isPluginListCommand(command)) {
            return false;
        }

        if (config.shouldBlockNonMinecraftNamespaces() && command.contains(":")) {
            return true;
        }

        for (String blocked : config.getBlockedCommands()) {
            if (matchesBlockedCommand(command, blocked)) {
                return true;
            }
        }

        return false;
    }

    private boolean isInternalPluginCommand(String command) {
        if (command.equals("pluginspoofer") || command.equals("ps") ||
            command.equals("pluginspoofer:pluginspoofer") || command.equals("pluginspoofer:ps")) {
            return true;
        }

        int namespaceIndex = command.indexOf(':');
        if (namespaceIndex >= 0 && namespaceIndex + 1 < command.length()) {
            String withoutNamespace = command.substring(namespaceIndex + 1);
            return withoutNamespace.equals("pluginspoofer") || withoutNamespace.equals("ps");
        }

        return false;
    }

    private void rewriteBlockedCommandToUnknown(PlayerCommandPreprocessEvent event, String blockedCommandLabel) {
        String normalizedCommand = blockedCommandLabel == null ? "" : blockedCommandLabel.trim().toLowerCase(Locale.ROOT);
        if (normalizedCommand.isEmpty()) {
            normalizedCommand = "pluginspoofer_blocked";
        }

        String originalCommandLabel = extractRawCommandLabel(event.getMessage());
        if (originalCommandLabel.isEmpty()) {
            originalCommandLabel = normalizedCommand;
        }

        String markerCommand = UnknownCommandRewriteTracker.register(event.getPlayer().getUniqueId(), originalCommandLabel);
        String rewrittenMessage = "/" + markerCommand + extractCommandArguments(event.getMessage());
        event.setMessage(rewrittenMessage);

        if (config.isDebugEnabled()) {
            plugin.getLogger().info("[Debug] Rewrite blocked command to let server handle unknown: " + rewrittenMessage);
        }
    }

    private String extractCommandArguments(String message) {
        if (message == null) {
            return "";
        }

        String withoutSlash = message.startsWith("/") ? message.substring(1) : message;
        int spaceIndex = withoutSlash.indexOf(' ');
        if (spaceIndex < 0) {
            return "";
        }

        return withoutSlash.substring(spaceIndex);
    }

    private String extractRawCommandLabel(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        String withoutSlash = message.startsWith("/") ? message.substring(1) : message;
        int spaceIndex = withoutSlash.indexOf(' ');
        if (spaceIndex >= 0) {
            return withoutSlash.substring(0, spaceIndex);
        }

        return withoutSlash;
    }
}
