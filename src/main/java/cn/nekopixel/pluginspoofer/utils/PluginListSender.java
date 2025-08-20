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
    private static final TextColor INFO_COLOR = TextColor.color(0x00BFFF);
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
        boolean hoverEnabled = config.isModernServerEnabled();
        boolean serverSupportsHover = ServerCompatibility.shouldUseHoverTooltips();
        
        boolean useHover = hoverEnabled && serverSupportsHover;
        
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
                if (hoverEnabled) {
                    player.sendMessage("ℹ Server Plugins (" + totalPlugins + "):");
                } else {
                    player.sendMessage("Server Plugins (" + totalPlugins + "):");
                }
            }
            
            if (config.isDebugEnabled() && hoverEnabled && !serverSupportsHover) {
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
        List<String> enabled = config.getBukkitEnabledPlugins();
        List<String> legacy = config.getBukkitLegacyPlugins();
        List<String> disabled = config.getBukkitDisabledPlugins();
        
        if (enabled.isEmpty() && legacy.isEmpty() && disabled.isEmpty()) {
            return;
        }
        
        Component bukkitTitle = Component.text("Bukkit Plugins:", NamedTextColor.GOLD);
        sendMessage(player, bukkitTitle);
        
        Component pluginLine = buildPluginLine(enabled, legacy, disabled);
        sendMessage(player, pluginLine);
    }
    
    private void sendPaperPlugins(Player player) {
        List<String> enabled = config.getPaperEnabledPlugins();
        List<String> legacy = config.getPaperLegacyPlugins();
        List<String> disabled = config.getPaperDisabledPlugins();
        
        if (enabled.isEmpty() && legacy.isEmpty() && disabled.isEmpty()) {
            return;
        }
        
        Component paperTitle = Component.text("Paper Plugins:", PAPER_COLOR);
        sendMessage(player, paperTitle);
        
        Component pluginLine = buildPluginLine(enabled, legacy, disabled);
        sendMessage(player, pluginLine);
    }
    
    private Component buildPluginLine(List<String> enabled, List<String> legacy, List<String> disabled) {
        Component lineComponent = Component.text(" - ", NamedTextColor.GRAY);
        boolean hoverEnabled = config.isModernServerEnabled();
        boolean serverSupportsHover = ServerCompatibility.shouldUseHoverTooltips();
        boolean useHover = hoverEnabled && serverSupportsHover;
        
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
                    Component legacyMarker = useHover
                        ? HoverTextBuilder.createLegacyMarker(NamedTextColor.YELLOW)
                        : Component.text("*", NamedTextColor.YELLOW);
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

    private boolean shouldUseLegacyFormat(Player player) {
        if (config.shouldForceLegacyFormat()) {
            if (config.isDebugEnabled()) {
                logger.info("[Debug] Force legacy format enabled for " + player.getName());
            }
            return true;
        }
        
        try {
            if (Bukkit.getPluginManager().getPlugin("ViaVersion") != null) {
                Class<?> viaAPI = Class.forName("com.viaversion.viaversion.api.Via");
                Object api = viaAPI.getMethod("getAPI").invoke(null);
                Method getPlayerVersion = api.getClass().getMethod("getPlayerVersion", Object.class);
                int protocolVersion = (int) getPlayerVersion.invoke(api, player);
                
                if (config.isDebugEnabled()) {
                    logger.info("[Debug] Player " + player.getName() + " protocol version: " + protocolVersion);
                }
                
                return protocolVersion < 393;
            }
            
            if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
                try {
                    Class<?> protocolManager = Class.forName("com.comphenix.protocol.ProtocolLibrary");
                    Object manager = protocolManager.getMethod("getProtocolManager").invoke(null);
                    Method getProtocolVersion = manager.getClass().getMethod("getProtocolVersion", Player.class);
                    Object version = getProtocolVersion.invoke(manager, player);
                    int protocolVersion = (int) version.getClass().getMethod("getVersion").invoke(version);
                    
                    if (config.isDebugEnabled()) {
                        logger.info("[Debug] Player " + player.getName() + " protocol version (ProtocolLib): " + protocolVersion);
                    }
                    
                    return protocolVersion < 393;
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            if (config.isDebugEnabled()) {
                logger.warning("[Debug] Failed to detect client version: " + e.getMessage());
            }
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
        
        List<String> bukkitEnabled = config.getBukkitEnabledPlugins();
        List<String> bukkitLegacy = config.getBukkitLegacyPlugins();
        List<String> bukkitDisabled = config.getBukkitDisabledPlugins();
        
        if (!bukkitEnabled.isEmpty() || !bukkitLegacy.isEmpty() || !bukkitDisabled.isEmpty()) {
            player.sendMessage("§6Bukkit Plugins:");
            String pluginLine = buildLegacyPluginLine(bukkitEnabled, bukkitLegacy, bukkitDisabled);
            player.sendMessage(pluginLine);
        }
        
        List<String> paperEnabled = config.getPaperEnabledPlugins();
        List<String> paperLegacy = config.getPaperLegacyPlugins();
        List<String> paperDisabled = config.getPaperDisabledPlugins();
        
        if (!paperEnabled.isEmpty() || !paperLegacy.isEmpty() || !paperDisabled.isEmpty()) {
            player.sendMessage("§3Paper Plugins:");
            String pluginLine = buildLegacyPluginLine(paperEnabled, paperLegacy, paperDisabled);
            player.sendMessage(pluginLine);
        }
    }

    private String buildLegacyPluginLine(List<String> enabled, List<String> legacy, List<String> disabled) {
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
        
        StringBuilder line = new StringBuilder("§7 - ");
        boolean first = true;
        
        for (PluginEntry entry : allPlugins) {
            if (!first) {
                line.append("§f, ");
            }
            
            switch (entry.type) {
                case ENABLED:
                    line.append("§a").append(entry.name);
                    break;
                case LEGACY:
                    line.append("§6*§a").append(entry.name);
                    break;
                case DISABLED:
                    line.append("§c").append(entry.name);
                    break;
            }
            
            first = false;
        }
        
        return line.toString();
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