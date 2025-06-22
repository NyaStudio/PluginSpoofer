package cn.nekopixel.pluginspoofer.listener;

import cn.nekopixel.pluginspoofer.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.ArrayList;
import java.util.List;

public class CommandListener implements Listener {
    private final ConfigManager config;
    
    public CommandListener(ConfigManager config) {
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase();
        String baseCommand = command.split(" ")[0];

        for (String blocked : config.getBlockedCommands()) {
            if (baseCommand.equals("/" + blocked.toLowerCase()) || 
                baseCommand.equals(blocked.toLowerCase())) {
                event.setCancelled(true);
                
                String cmdName = blocked.toLowerCase();
                if ((cmdName.equals("pl") || cmdName.equals("plugins") || 
                     cmdName.equals("bukkit:pl") || cmdName.equals("bukkit:plugins")) 
                     && config.isCustomPluginListEnabled()) {
                    sendCustomPluginList(event);
                } else {
                    event.getPlayer().sendMessage(config.getUnknownCommandMessage());
                }
                return;
            }
        }
    }
    
    private void sendCustomPluginList(PlayerCommandPreprocessEvent event) {
        List<String> paperPlugins = config.getPaperPlugins();
        List<String> bukkitPlugins = config.getBukkitPlugins();
        int totalPlugins = paperPlugins.size() + bukkitPlugins.size();
        
        event.getPlayer().sendMessage(ChatColor.DARK_AQUA + "ℹ" + ChatColor.WHITE + " Server Plugins (" + totalPlugins + "):");
        
        if (!bukkitPlugins.isEmpty()) {
            event.getPlayer().sendMessage(ChatColor.GOLD + "Bukkit Plugins:");
            String pluginList = String.join(", ", bukkitPlugins);
            event.getPlayer().sendMessage(ChatColor.GRAY + " - " + ChatColor.GREEN + pluginList);
        }
        
        if (!paperPlugins.isEmpty()) {
            event.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Paper Plugins:");
            String pluginList = String.join(", ", paperPlugins);
            event.getPlayer().sendMessage(ChatColor.GRAY + " - " + ChatColor.GREEN + pluginList);
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
}