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
    
    public List<String> getPaperPlugins() {
        return config.getStringList("plugins.paper");
    }
    
    public List<String> getBukkitPlugins() {
        return config.getStringList("plugins.bukkit");
    }
    
    public String getUnknownCommandMessage() {
        return config.getString("unknown-msg", "Unknown command. Type \"/help\" for help.");
    }
} 