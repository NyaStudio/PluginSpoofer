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
        String command = event.getMessage().toLowerCase();
        String baseCommand = command.split(" ")[0];

        for (String blocked : config.getBlockedCommands()) {
            if (baseCommand.equals("/" + blocked.toLowerCase()) || 
                baseCommand.equals(blocked.toLowerCase())) {
                
                if (config.isDebugEnabled()) {
                    plugin.getLogger().info("[Debug] Caught Command: " + event.getMessage());
                }
                
                event.setCancelled(true);
                
                String cmdName = blocked.toLowerCase();
                if ((cmdName.equals("pl") || cmdName.equals("plugins") || 
                     cmdName.equals("bukkit:pl") || cmdName.equals("bukkit:plugins")) 
                     && config.isCustomPluginListEnabled()) {

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
        event.getCommands().removeIf(cmd -> cmd.contains(":"));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTabComplete(TabCompleteEvent event) {
        String buffer = event.getBuffer().toLowerCase();

        if (buffer.equals("/") && config.shouldBlockSlashCompletion()) {
            event.setCompletions(new ArrayList<>());
            return;
        }

        for (String blocked : config.getBlockedCommands()) {
            String blockedLower = blocked.toLowerCase();
            if (!blockedLower.startsWith("/")) {
                blockedLower = "/" + blockedLower;
            }
            
            if (buffer.startsWith(blockedLower.substring(0, Math.min(blockedLower.length(), buffer.length())))) {
                event.setCancelled(true);
                event.setCompletions(new ArrayList<>());
                return;
            }
        }
        
        if (config.shouldBlockNonMinecraftNamespaces() && buffer.contains(":") && !event.isCancelled()) {
            String[] parts = buffer.split(":");
            if (parts.length > 0) {
                String namespace = parts[0].replace("/", "");
                if (!namespace.equals("minecraft")) {
                    event.setCancelled(true);
                    event.setCompletions(new ArrayList<>());
                }
            }
        }
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