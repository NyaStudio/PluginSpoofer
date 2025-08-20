package cn.nekopixel.pluginspoofer.command;

import cn.nekopixel.pluginspoofer.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler implements CommandExecutor, TabCompleter {
    private final Main plugin;
    
    public CommandHandler(Main plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("pluginspoofer.admin")) {
            sender.sendMessage(ChatColor.RED + "你没有使用此命令的权限");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.getConfigManager().loadConfig();
                sender.sendMessage(ChatColor.GREEN + "PluginSpoofer 配置已重载！");
                break;
            case "version", "ver":
                VersionCommand.showVersion(sender, plugin);
                break;
            case "help":
                sendHelp(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "未知的子命令: " + args[0]);
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "========== PluginSpoofer 帮助 ==========");
        sender.sendMessage(ChatColor.YELLOW + "/ps reload" + ChatColor.WHITE + " - 重载配置文件");
        sender.sendMessage(ChatColor.YELLOW + "/ps version" + ChatColor.WHITE + " - 显示版本信息");
        sender.sendMessage(ChatColor.YELLOW + "/ps help" + ChatColor.WHITE + " - 显示此帮助信息");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("pluginspoofer.admin")) {
            return new ArrayList<>();
        }
        
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = List.of("reload", "version", "ver", "help");
            String input = args[0].toLowerCase();
            
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(input)) {
                    completions.add(subCommand);
                }
            }
        }
        
        return completions;
    }
} 