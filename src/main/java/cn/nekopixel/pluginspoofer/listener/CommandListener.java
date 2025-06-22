package cn.nekopixel.pluginspoofer.listener;

import cn.nekopixel.pluginspoofer.config.ConfigManager;
import cn.nekopixel.pluginspoofer.utils.PluginListSender;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.ArrayList;

public class CommandListener implements Listener {
    private final ConfigManager config;
    private final PluginListSender pluginListSender;
    
    public CommandListener(ConfigManager config, BukkitAudiences adventure) {
        this.config = config;
        this.pluginListSender = new PluginListSender(config, adventure);
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
                    pluginListSender.sendCustomPluginList(event.getPlayer());
                } else {
                    event.getPlayer().sendMessage(config.getUnknownCommandMessage());
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
}