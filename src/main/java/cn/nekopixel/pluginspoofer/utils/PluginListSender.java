package cn.nekopixel.pluginspoofer.utils;

import cn.nekopixel.pluginspoofer.config.ConfigManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import org.bukkit.Bukkit;

public class PluginListSender {
    private static final TextColor PAPER_COLOR = TextColor.color(0x00AAAA);

    private final ConfigManager config;
    private final BukkitAudiences adventure;
    private final Logger logger;
    
    public PluginListSender(ConfigManager config, BukkitAudiences adventure) {
        this.config = config;
        this.adventure = adventure;
        this.logger = Logger.getLogger("PluginSpoofer");
    }
    
    public void sendCustomPluginList(Player player) {
        if (config.isDebugEnabled()) {
            logger.info("sendCustomPluginList called by: " + player.getName());
        }
        
        int totalPlugins = getTotalPluginCount();
        boolean modernServer = config.isModernServerEnabled();
        boolean serverSupportsHover = ServerCompatibility.shouldUseHoverTooltips();
        
        boolean useHover = modernServer && serverSupportsHover;

        boolean useLegacyFormat = shouldUseLegacyFormat(player);
        
        if (useLegacyFormat) {
            sendLegacyPluginList(player);
            return;
        }

        Component title;
        if (useHover) {
            title = Component.text()
                .append(HoverTextBuilder.createInfoIcon())
                .append(Component.text(" Server Plugins (" + totalPlugins + "):", NamedTextColor.WHITE))
                .build();
        } else {
            title = Component.text()
                .append(Component.text("Server Plugins (" + totalPlugins + "):", NamedTextColor.WHITE))
                .build();
        }
        
        try {
            if (ServerCompatibility.isPaper() && adventure == null) {
                sendMessage(player, title);
            } else if (adventure != null) {
                adventure.player(player).sendMessage(title);
            } else {
                if (modernServer) {
                    player.sendMessage("ℹ Server Plugins (" + totalPlugins + "):");
                } else {
                    player.sendMessage("Server Plugins (" + totalPlugins + "):");
                }
            }
            
            if (config.isDebugEnabled() && modernServer && !serverSupportsHover) {
                logger.warning("Your server doesn't support hover tooltips natively.");
                logger.warning("Consider using Paper 1.16.5+ for full hover support.");
            }
        } catch (Exception e) {
            if (config.isDebugEnabled()) {
                logger.severe("Failed to send message with Adventure API: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        sendBukkitPlugins(player);
        sendPaperPlugins(player);
    }
    
    private void sendBukkitPlugins(Player player) {
        sendPluginCategory(player, "Bukkit Plugins:", NamedTextColor.GOLD,
            config.getBukkitEnabledPlugins(),
            config.getBukkitLegacyPlugins(),
            config.getBukkitDisabledPlugins());
    }
    
    private void sendPaperPlugins(Player player) {
        sendPluginCategory(player, "Paper Plugins:", PAPER_COLOR,
            config.getPaperEnabledPlugins(),
            config.getPaperLegacyPlugins(),
            config.getPaperDisabledPlugins());
    }
    
    private void sendPluginCategory(Player player, String title, TextColor titleColor,
                                  List<String> enabled, List<String> legacy, List<String> disabled) {
        if (enabled.isEmpty() && legacy.isEmpty() && disabled.isEmpty()) {
            return;
        }
        
        Component titleComponent = Component.text(title, titleColor);
        sendMessage(player, titleComponent);
        
        Component pluginLine = buildPluginLine(enabled, legacy, disabled);
        sendMessage(player, pluginLine);
    }
    
    private Component buildPluginLine(List<String> enabled, List<String> legacy, List<String> disabled) {
        boolean useHover = shouldUseHoverFeatures();
        List<PluginEntry> allPlugins = createSortedPluginList(enabled, legacy, disabled);
        
        Component lineComponent = Component.text(" - ", NamedTextColor.GRAY);
        boolean first = true;
        
        for (PluginEntry entry : allPlugins) {
            if (!first) {
                lineComponent = lineComponent.append(Component.text(", ", NamedTextColor.WHITE));
            }
            
            lineComponent = lineComponent.append(createPluginComponent(entry, useHover));
            first = false;
        }
        
        return lineComponent;
    }
    
    private Component createPluginComponent(PluginEntry entry, boolean useHover) {
        switch (entry.type) {
            case ENABLED:
                return Component.text(entry.name, NamedTextColor.GREEN);
            case LEGACY:
                Component legacyMarker = useHover
                    ? HoverTextBuilder.createLegacyMarker(NamedTextColor.YELLOW)
                    : Component.text("*", NamedTextColor.YELLOW);
                return legacyMarker.append(Component.text(entry.name, NamedTextColor.GREEN));
            case DISABLED:
                return Component.text(entry.name, NamedTextColor.RED);
            default:
                return Component.text(entry.name, NamedTextColor.WHITE);
        }
    }
    
    private boolean shouldUseHoverFeatures() {
        return config.isModernServerEnabled() && ServerCompatibility.shouldUseHoverTooltips();
    }
    
    private List<PluginEntry> createSortedPluginList(List<String> enabled, List<String> legacy, List<String> disabled) {
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
        return allPlugins;
    }

    private boolean shouldUseLegacyFormat(Player player) {
        if (config.shouldForceLegacyFormat()) {
            if (config.isDebugEnabled()) {
                logger.info("[Debug] Force legacy format enabled for " + player.getName());
            }
            return true;
        }
        
        Integer protocolVersion = getPlayerProtocolVersion(player);
        if (protocolVersion != null) {
            return protocolVersion < 393;  // 1.13
        }
        
        String serverVersion = Bukkit.getVersion();
        if (serverVersion.contains("1.20") || serverVersion.contains("1.21")) {
            if (config.isDebugEnabled()) {
                logger.info("[Debug] High version server detected, using legacy format for safety");
            }
            return true;
        }
        
        return false;
    }
    
    private Integer getPlayerProtocolVersion(Player player) {
        try {
            // try ViaVersion
            if (Bukkit.getPluginManager().getPlugin("ViaVersion") != null) {
                Integer version = getViaVersionProtocol(player);
                if (version != null) {
                    if (config.isDebugEnabled()) {
                        logger.info("[Debug] Player " + player.getName() + " protocol version: " + version);
                    }
                    return version;
                }
            }
            
            // try ProtocolLib
            if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
                Integer version = getProtocolLibVersion(player);
                if (version != null) {
                    if (config.isDebugEnabled()) {
                        logger.info("[Debug] Player " + player.getName() + " protocol version (ProtocolLib): " + version);
                    }
                    return version;
                }
            }
        } catch (Exception e) {
            if (config.isDebugEnabled()) {
                logger.warning("[Debug] Failed to detect client version: " + e.getMessage());
            }
        }
        
        return null;
    }
    
    private Integer getViaVersionProtocol(Player player) {
        try {
            Class<?> viaAPI = Class.forName("com.viaversion.viaversion.api.Via");
            Object api = viaAPI.getMethod("getAPI").invoke(null);
            Method getPlayerVersion = api.getClass().getMethod("getPlayerVersion", Object.class);
            return (Integer) getPlayerVersion.invoke(api, player);
        } catch (Exception e) {
            return null;
        }
    }
    
    private Integer getProtocolLibVersion(Player player) {
        try {
            Class<?> protocolManager = Class.forName("com.comphenix.protocol.ProtocolLibrary");
            Object manager = protocolManager.getMethod("getProtocolManager").invoke(null);
            Method getProtocolVersion = manager.getClass().getMethod("getProtocolVersion", Player.class);
            Object version = getProtocolVersion.invoke(manager, player);
            return (Integer) version.getClass().getMethod("getVersion").invoke(version);
        } catch (Exception e) {
            return null;
        }
    }

    private void sendLegacyPluginList(Player player) {
        if (config.isDebugEnabled()) {
            logger.info("[Debug] use legacy type send to: " + player.getName());
        }
        
        int totalPlugins = getTotalPluginCount();
        
        if (config.isModernServerEnabled()) {
            player.sendMessage("§3ℹ§f Server Plugins (" + totalPlugins + "):");
        } else {
            player.sendMessage("§fServer Plugins (" + totalPlugins + "):");
        }
        
        sendLegacyPluginCategory(player, "§6Bukkit Plugins:",
            config.getBukkitEnabledPlugins(),
            config.getBukkitLegacyPlugins(),
            config.getBukkitDisabledPlugins());
        
        sendLegacyPluginCategory(player, "§3Paper Plugins:",
            config.getPaperEnabledPlugins(),
            config.getPaperLegacyPlugins(),
            config.getPaperDisabledPlugins());
    }
    
    private void sendLegacyPluginCategory(Player player, String title,
                                        List<String> enabled, List<String> legacy, List<String> disabled) {
        if (enabled.isEmpty() && legacy.isEmpty() && disabled.isEmpty()) {
            return;
        }
        
        player.sendMessage(title);
        String pluginLine = buildLegacyPluginLine(enabled, legacy, disabled);
        player.sendMessage(pluginLine);
    }

    private String buildLegacyPluginLine(List<String> enabled, List<String> legacy, List<String> disabled) {
        List<PluginEntry> allPlugins = createSortedPluginList(enabled, legacy, disabled);
        
        StringBuilder line = new StringBuilder("§7 - ");
        boolean first = true;
        
        for (PluginEntry entry : allPlugins) {
            if (!first) {
                line.append("§f, ");
            }
            
            line.append(createLegacyPluginText(entry));
            first = false;
        }
        
        return line.toString();
    }
    
    private String createLegacyPluginText(PluginEntry entry) {
        switch (entry.type) {
            case ENABLED:
                return "§a" + entry.name;
            case LEGACY:
                return "§6*§a" + entry.name;
            case DISABLED:
                return "§c" + entry.name;
            default:
                return "§f" + entry.name;
        }
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

    private void sendMessage(Player player, Component message) {
        try {
            if (ServerCompatibility.isPaper() && adventure == null) {
                try {
                    Method sendMessageMethod = player.getClass().getMethod("sendMessage", Component.class);
                    sendMessageMethod.invoke(player, message);
                } catch (Exception e) {
                    sendLegacyMessage(player, message);
                }
            } else if (adventure != null) {
                adventure.player(player).sendMessage(message);
            } else {
                sendLegacyMessage(player, message);
            }
        } catch (Exception e) {
            sendLegacyMessage(player, message);
        }
    }

    private void sendLegacyMessage(Player player, Component message) {
        try {
            String legacyText = LegacyComponentSerializer.legacySection().serialize(message);
            player.sendMessage(legacyText);
        } catch (Exception e) {
            player.sendMessage(message.toString());
        }
    }
} 