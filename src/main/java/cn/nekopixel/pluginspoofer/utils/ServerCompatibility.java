package cn.nekopixel.pluginspoofer.utils;

import org.bukkit.Bukkit;

public class ServerCompatibility {
    
    private static Boolean isPaper = null;
    private static Boolean hasNativeAdventure = null;

    public static boolean isPaper() {
        if (isPaper == null) {
            try {
                Class.forName("com.destroystokyo.paper.PaperConfig");
                isPaper = true;
            } catch (ClassNotFoundException e) {
                try {
                    Class.forName("io.papermc.paper.configuration.Configuration");
                    isPaper = true;
                } catch (ClassNotFoundException e2) {
                    isPaper = false;
                }
            }
        }
        return isPaper;
    }

    public static boolean hasNativeAdventureSupport() {
        if (hasNativeAdventure == null) {
            try {
                Class.forName("io.papermc.paper.adventure.PaperAdventure");
                hasNativeAdventure = true;
            } catch (ClassNotFoundException e) {
                hasNativeAdventure = false;
            }
        }
        return hasNativeAdventure;
    }

    public static String getServerType() {
        String version = Bukkit.getVersion();
        if (version.contains("Paper")) {
            return "Paper";
        } else if (version.contains("Purpur")) {
            return "Purpur";
        } else if (version.contains("Spigot")) {
            return "Spigot";
        } else if (version.contains("CraftBukkit")) {
            return "CraftBukkit";
        } else {
            return "Unknown";
        }
    }

    public static boolean shouldUseHoverTooltips() {
        return isPaper() && hasNativeAdventureSupport();
    }
} 