package cn.nekopixel.pluginspoofer.utils;

import cn.nekopixel.pluginspoofer.config.ConfigManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class PluginListSender {
    private final ConfigManager config;
    private final BukkitAudiences adventure;
    
    private static final TextColor BUKKIT_COLOR = TextColor.fromHexString("#ea7f06");
    private static final TextColor INFO_COLOR = TextColor.fromHexString("#339dd7");
    private static final TextColor LEGACY_COLOR = BUKKIT_COLOR;
    
    public PluginListSender(ConfigManager config, BukkitAudiences adventure) {
        this.config = config;
        this.adventure = adventure;
    }
    
    public void sendCustomPluginList(Player player) {
        int totalPlugins = getTotalPluginCount();

        Component title = Component.text("ℹ", INFO_COLOR)
            .append(Component.text(" Server Plugins (" + totalPlugins + "):", NamedTextColor.WHITE));
        adventure.player(player).sendMessage(title);
        
        sendBukkitPlugins(player);
        sendPaperPlugins(player);
    }
    
    private void sendBukkitPlugins(Player player) {
        List<String> enabled = config.getBukkitEnabledPlugins();
        List<String> legacy = config.getBukkitLegacyPlugins();
        List<String> disabled = config.getBukkitDisabledPlugins();
        
        if (enabled.isEmpty() && legacy.isEmpty() && disabled.isEmpty()) {
            return;
        }
        
        Component bukkitTitle = Component.text("Bukkit Plugins:", BUKKIT_COLOR);
        adventure.player(player).sendMessage(bukkitTitle);
        
        Component pluginLine = buildPluginLine(enabled, legacy, disabled);
        adventure.player(player).sendMessage(pluginLine);
    }
    
    private void sendPaperPlugins(Player player) {
        List<String> enabled = config.getPaperEnabledPlugins();
        List<String> legacy = config.getPaperLegacyPlugins();
        List<String> disabled = config.getPaperDisabledPlugins();
        
        if (enabled.isEmpty() && legacy.isEmpty() && disabled.isEmpty()) {
            return;
        }
        
        Component paperTitle = Component.text("Paper Plugins:", NamedTextColor.DARK_AQUA);
        adventure.player(player).sendMessage(paperTitle);
        
        Component pluginLine = buildPluginLine(enabled, legacy, disabled);
        adventure.player(player).sendMessage(pluginLine);
    }
    
    private Component buildPluginLine(List<String> enabled, List<String> legacy, List<String> disabled) {
        Component lineComponent = Component.text(" - ", NamedTextColor.GRAY);
        
        List<String> sortedEnabled = new ArrayList<>(enabled);
        Collections.sort(sortedEnabled, String.CASE_INSENSITIVE_ORDER);
        
        List<String> sortedLegacy = new ArrayList<>(legacy);
        Collections.sort(sortedLegacy, String.CASE_INSENSITIVE_ORDER);
        
        List<String> sortedDisabled = new ArrayList<>(disabled);
        Collections.sort(sortedDisabled, String.CASE_INSENSITIVE_ORDER);
        
        boolean first = true;
        
        for (String plugin : sortedEnabled) {
            if (!first) {
                lineComponent = lineComponent.append(Component.text(", ", NamedTextColor.WHITE));
            }
            lineComponent = lineComponent.append(Component.text(plugin, NamedTextColor.GREEN));
            first = false;
        }
        
        for (String plugin : sortedLegacy) {
            if (!first) {
                lineComponent = lineComponent.append(Component.text(", ", NamedTextColor.WHITE));
            }
            lineComponent = lineComponent.append(Component.text("*", LEGACY_COLOR))
                                         .append(Component.text(plugin, NamedTextColor.GREEN));
            first = false;
        }
        
        for (String plugin : sortedDisabled) {
            if (!first) {
                lineComponent = lineComponent.append(Component.text(", ", NamedTextColor.WHITE));
            }
            lineComponent = lineComponent.append(Component.text(plugin, NamedTextColor.RED));
            first = false;
        }
        
        return lineComponent;
    }
    
    private int getTotalPluginCount() {
        return config.getBukkitEnabledPlugins().size() +
               config.getBukkitLegacyPlugins().size() +
               config.getBukkitDisabledPlugins().size() +
               config.getPaperEnabledPlugins().size() +
               config.getPaperLegacyPlugins().size() +
               config.getPaperDisabledPlugins().size();
    }
} 