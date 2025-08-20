package cn.nekopixel.pluginspoofer.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class ModernMessageBuilder {
    
    public static Component createModernUnknownCommandMessage(String originalCommand) {
        String commandWithoutSlash = originalCommand.startsWith("/") ? originalCommand.substring(1) : originalCommand;
        Component firstLine = Component.text("未知或不完整的命令，错误见下：", NamedTextColor.RED);
        
        Component commandPart = Component.text(commandWithoutSlash, NamedTextColor.RED)
            .decoration(TextDecoration.UNDERLINED, true);
        Component errorPointer = Component.text("<--[此处]", NamedTextColor.RED)
            .decoration(TextDecoration.ITALIC, true);
        
        Component secondLine = Component.text()
            .append(commandPart)
            .append(errorPointer)
            .clickEvent(ClickEvent.suggestCommand(originalCommand))
            .build();
        
        return Component.text()
            .append(firstLine)
            .append(Component.newline())
            .append(secondLine)
            .build();
    }
    
    public static Component createLegacyUnknownCommandMessage() {
        return Component.text("Unknown command. Type \"/help\" for help.", NamedTextColor.WHITE);
    }
}