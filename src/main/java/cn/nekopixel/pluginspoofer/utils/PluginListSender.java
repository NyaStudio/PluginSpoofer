package cn.nekopixel.pluginspoofer.utils;

import cn.nekopixel.pluginspoofer.config.ConfigManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class PluginListSender {
    private final ConfigManager config;
    private final BukkitAudiences adventure;
    
    public PluginListSender(ConfigManager config, BukkitAudiences adventure) {
        this.config = config;
        this.adventure = adventure;
    }
    
    public void sendCustomPluginList(Player player) {
        List<String> paperPlugins = config.getPaperPlugins();
        List<String> bukkitPlugins = config.getBukkitPlugins();
        int totalPlugins = paperPlugins.size() + bukkitPlugins.size();
        
        player.sendMessage(ChatColor.DARK_AQUA + "ℹ" + ChatColor.WHITE + " Server Plugins (" + totalPlugins + "):");
        
        if (!bukkitPlugins.isEmpty()) {
            Component bukkitTitle = Component.text("Bukkit Plugins:")
                .color(TextColor.fromHexString("#ea7f06"));
            adventure.player(player).sendMessage(bukkitTitle);
            
            String pluginList = String.join(ChatColor.WHITE + ", " + ChatColor.GREEN, bukkitPlugins);
            player.sendMessage(ChatColor.GRAY + " - " + ChatColor.GREEN + pluginList);
        }
        
        if (!paperPlugins.isEmpty()) {
            Component paperTitle = Component.text("Paper Plugins:")
                    .color(TextColor.fromHexString("#339dd7"));
            adventure.player(player).sendMessage(paperTitle);

            String pluginList = String.join(ChatColor.WHITE + ", " + ChatColor.GREEN, paperPlugins);
            player.sendMessage(ChatColor.GRAY + " - " + ChatColor.GREEN + pluginList);
        }
    }
} 