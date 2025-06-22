package cn.nekopixel.pluginspoofer.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

public class ConfigManager {
    private final Plugin plugin;
    private FileConfiguration config;
    
    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (configFile.exists()) {
            this.config = YamlConfiguration.loadConfiguration(configFile);
        } else {
            plugin.reloadConfig();
            this.config = plugin.getConfig();
        }
    }
    
    public boolean isDebugEnabled() {
        return config.getBoolean("debug", false);
    }
    
    public List<String> getBlockedCommands() {
        return config.getStringList("blocked-commands");
    }
    
    public boolean shouldBlockSlashCompletion() {
        return true;
    }
    
    public boolean shouldBlockNonMinecraftNamespaces() {
        return true;
    }
    
    public boolean isCustomPluginListEnabled() {
        return config.getBoolean("plugins.enabled", true);
    }
    
    public boolean isHoverTooltipsEnabled() {
        return config.getBoolean("plugins.hover-tooltips", true);
    }
    
    public List<String> getPaperEnabledPlugins() {
        if (!config.contains("plugins.paper") || !config.contains("plugins.paper.enabled")) {
            return new ArrayList<>();
        }
        return config.getStringList("plugins.paper.enabled");
    }
    
    public List<String> getPaperLegacyPlugins() {
        if (!config.contains("plugins.paper") || !config.contains("plugins.paper.legacy")) {
            return new ArrayList<>();
        }
        return config.getStringList("plugins.paper.legacy");
    }
    
    public List<String> getPaperDisabledPlugins() {
        if (!config.contains("plugins.paper") || !config.contains("plugins.paper.disabled")) {
            return new ArrayList<>();
        }
        return config.getStringList("plugins.paper.disabled");
    }
    
    public List<String> getBukkitEnabledPlugins() {
        if (!config.contains("plugins.bukkit") || !config.contains("plugins.bukkit.enabled")) {
            return new ArrayList<>();
        }
        return config.getStringList("plugins.bukkit.enabled");
    }
    
    public List<String> getBukkitLegacyPlugins() {
        if (!config.contains("plugins.bukkit") || !config.contains("plugins.bukkit.legacy")) {
            return new ArrayList<>();
        }
        return config.getStringList("plugins.bukkit.legacy");
    }
    
    public List<String> getBukkitDisabledPlugins() {
        if (!config.contains("plugins.bukkit") || !config.contains("plugins.bukkit.disabled")) {
            return new ArrayList<>();
        }
        return config.getStringList("plugins.bukkit.disabled");
    }
    
    @Deprecated
    public List<String> getPaperPlugins() {
        return getPaperEnabledPlugins();
    }
    
    @Deprecated
    public List<String> getBukkitPlugins() {
        return getBukkitEnabledPlugins();
    }
    
    public String getUnknownCommandMessage() {
        return config.getString("unknown-msg", "Unknown command. Type \"/help\" for help.");
    }
} 