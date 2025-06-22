package cn.nekopixel.pluginspoofer.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class ConfigManager {
    private final Plugin plugin;
    private FileConfiguration config;
    
    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
    
    public boolean isDebugEnabled() {
        return config.getBoolean("debug", true);
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
    
    public List<String> getPaperEnabledPlugins() {
        return config.getStringList("plugins.paper.enabled");
    }
    
    public List<String> getPaperLegacyPlugins() {
        return config.getStringList("plugins.paper.legacy");
    }
    
    public List<String> getPaperDisabledPlugins() {
        return config.getStringList("plugins.paper.disabled");
    }
    
    public List<String> getBukkitEnabledPlugins() {
        return config.getStringList("plugins.bukkit.enabled");
    }
    
    public List<String> getBukkitLegacyPlugins() {
        return config.getStringList("plugins.bukkit.legacy");
    }
    
    public List<String> getBukkitDisabledPlugins() {
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