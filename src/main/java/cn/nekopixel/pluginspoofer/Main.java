package cn.nekopixel.pluginspoofer;

import cn.nekopixel.pluginspoofer.command.CommandHandler;
import cn.nekopixel.pluginspoofer.config.ConfigManager;
import cn.nekopixel.pluginspoofer.listener.CommandListener;
import cn.nekopixel.pluginspoofer.debug.DebugPacketListener;
import cn.nekopixel.pluginspoofer.utils.ServerCompatibility;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    
    private ConfigManager configManager;
    private BukkitAudiences adventure;
    
    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }
    
    @Override
    public void onEnable() {
        if (!ServerCompatibility.isPaper()) {
            adventure = BukkitAudiences.create(this);
            getLogger().info("Using BukkitAudiences for Adventure API");
        } else {
            getLogger().info("Using Paper's native Adventure API");
        }
        
        configManager = new ConfigManager(this);
        PacketEvents.getAPI().init();
        getServer().getPluginManager().registerEvents(new CommandListener(configManager, adventure), this);
        PacketEvents.getAPI().getEventManager().registerListener(new DebugPacketListener(this, configManager));
        
        CommandHandler commandHandler = new CommandHandler(this);
        getCommand("pluginspoofer").setExecutor(commandHandler);
        getCommand("pluginspoofer").setTabCompleter(commandHandler);
        
        getLogger().info("加载完成！");
    }
    
    @Override
    public void onDisable() {
        if (adventure != null) {
            adventure.close();
        }
        PacketEvents.getAPI().terminate();
        getLogger().info("卸载完成！");
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public BukkitAudiences getAdventure() {
        return adventure;
    }
}
