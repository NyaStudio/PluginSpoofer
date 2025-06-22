package cn.nekopixel.pluginspoofer.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class HoverTextBuilder {
    
    private static final TextColor INFO_COLOR = TextColor.fromHexString("#339dd7");
    public static Component createInfoIcon() {
        Component hoverText = Component.text()
            .append(Component.text("ℹ What is a server plugin?", INFO_COLOR))
            .append(Component.newline())
            .append(Component.text("Server plugins can add new behavior to your server!", NamedTextColor.WHITE))
            .append(Component.newline())
            .append(Component.text("You can find new plugins on Paper's plugin repository, Hangar.", NamedTextColor.WHITE))
            .append(Component.newline())
            .append(Component.newline())
            .append(Component.text("https://hangar.papermc.io/", NamedTextColor.WHITE))
            .build();
        
        return Component.text("ℹ", INFO_COLOR)
            .hoverEvent(HoverEvent.showText(hoverText));
    }

    public static Component createLegacyMarker(TextColor markerColor) {
        Component hoverText = Component.text()
            .append(Component.text("ℹ What is a legacy plugin?", INFO_COLOR))
            .append(Component.newline())
            .append(Component.text("A legacy plugin is a plugin that was made on", NamedTextColor.WHITE))
            .append(Component.newline())
            .append(Component.text("very old unsupported versions of the game.", NamedTextColor.WHITE))
            .append(Component.newline())
            .append(Component.newline())
            .append(Component.text("It is encouraged that you replace this plugin,", NamedTextColor.WHITE))
            .append(Component.newline())
            .append(Component.text("as they might not work in the future and may cause", NamedTextColor.WHITE))
            .append(Component.newline())
            .append(Component.text("performance issues.", NamedTextColor.WHITE))
            .build();
        
        return Component.text("*", markerColor)
            .hoverEvent(HoverEvent.showText(hoverText));
    }
} 