package cn.nekopixel.pluginspoofer.utils;

import cn.nekopixel.pluginspoofer.config.ConfigManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;

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
        boolean hoverEnabled = config.isHoverTooltipsEnabled();
        
        if (config.isDebugEnabled()) {
            player.sendMessage("§7[Debug] Hover tooltips enabled: " + hoverEnabled);
            player.sendMessage("§7[Debug] Adventure instance: " + (adventure != null ? "OK" : "NULL"));
        }

        Component infoIcon = hoverEnabled 
            ? HoverTextBuilder.createInfoIcon()
            : Component.text("ℹ", INFO_COLOR);
            
        Component title = infoIcon
            .append(Component.text(" Server Plugins (" + totalPlugins + "):", NamedTextColor.WHITE));
        
        try {
            adventure.player(player).sendMessage(title);
            
            if (config.isDebugEnabled() && hoverEnabled) {
                Component testHover = Component.text("§7[Debug] Hover test: ")
                    .append(Component.text("Hover me!", NamedTextColor.GREEN)
                        .hoverEvent(HoverEvent.showText(Component.text("If you see this, hover is working!", NamedTextColor.YELLOW))));
                adventure.player(player).sendMessage(testHover);
            }
        } catch (Exception e) {
            player.sendMessage("§c[Error] Failed to send message with Adventure API: " + e.getMessage());
            if (config.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
        
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
        boolean hoverEnabled = config.isHoverTooltipsEnabled();
        
        List<PluginEntry> allPlugins = new ArrayList<>();
        for (String plugin : enabled) {
            allPlugins.add(new PluginEntry(plugin, PluginType.ENABLED));
        }
        for (String plugin : legacy) {
            allPlugins.add(new PluginEntry(plugin, PluginType.LEGACY));
        }
        for (String plugin : disabled) {
            allPlugins.add(new PluginEntry(plugin, PluginType.DISABLED));
        }
        
        allPlugins.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
        boolean first = true;
        
        for (PluginEntry entry : allPlugins) {
            if (!first) {
                lineComponent = lineComponent.append(Component.text(", ", NamedTextColor.WHITE));
            }
            
            switch (entry.type) {
                case ENABLED:
                    lineComponent = lineComponent.append(Component.text(entry.name, NamedTextColor.GREEN));
                    break;
                case LEGACY:
                    Component legacyMarker = hoverEnabled
                        ? HoverTextBuilder.createLegacyMarker(LEGACY_COLOR)
                        : Component.text("*", LEGACY_COLOR);
                    lineComponent = lineComponent.append(legacyMarker)
                                                 .append(Component.text(entry.name, NamedTextColor.GREEN));
                    break;
                case DISABLED:
                    lineComponent = lineComponent.append(Component.text(entry.name, NamedTextColor.RED));
                    break;
            }
            
            first = false;
        }
        
        return lineComponent;
    }
    
    private static class PluginEntry {
        final String name;
        final PluginType type;
        
        PluginEntry(String name, PluginType type) {
            this.name = name;
            this.type = type;
        }
    }
    
    private enum PluginType {
        ENABLED, LEGACY, DISABLED
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