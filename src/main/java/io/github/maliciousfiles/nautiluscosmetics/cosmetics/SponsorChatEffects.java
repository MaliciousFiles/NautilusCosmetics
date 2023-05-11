package io.github.maliciousfiles.nautiluscosmetics.cosmetics;

import io.github.maliciousfiles.nautiluscosmetics.NautilusCosmetics;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.ChatFormatting;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Map;

public class SponsorChatEffects implements Listener {

    private static final Map<String, ChatFormatting> NAMES = Map.ofEntries(
            Map.entry("white", ChatFormatting.WHITE),
            Map.entry("dark_red", ChatFormatting.DARK_RED),
            Map.entry("red", ChatFormatting.RED),
            Map.entry("gold", ChatFormatting.GOLD),
            Map.entry("yellow", ChatFormatting.YELLOW),
            Map.entry("dark_green", ChatFormatting.DARK_GREEN),
            Map.entry("green", ChatFormatting.GREEN),
            Map.entry("dark_aqua", ChatFormatting.DARK_AQUA),
            Map.entry("aqua", ChatFormatting.AQUA),
            Map.entry("dark_blue", ChatFormatting.DARK_BLUE),
            Map.entry("blue", ChatFormatting.BLUE),
            Map.entry("dark_purple", ChatFormatting.DARK_PURPLE),
            Map.entry("light_purple", ChatFormatting.LIGHT_PURPLE),
            Map.entry("dark_gray", ChatFormatting.DARK_GRAY),
            Map.entry("gray", ChatFormatting.GRAY),
            Map.entry("black", ChatFormatting.BLACK),
            Map.entry("bold", ChatFormatting.BOLD),
            Map.entry("italic", ChatFormatting.ITALIC),
            Map.entry("strikethrough", ChatFormatting.STRIKETHROUGH),
            Map.entry("underline", ChatFormatting.UNDERLINE),
            Map.entry("obfuscated", ChatFormatting.OBFUSCATED),
            Map.entry("reset", ChatFormatting.RESET));

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        if (event.getPlayer().hasPermission("nautiluscosmetics.chat.chatcodes")) {
            String content = NautilusCosmetics.getTextContent(event.message());
            Component message = Component.empty();

            Component building = Component.empty();

            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);

                boolean formatted = false;
                if (c == '`' && i < content.length() - 1 && (i == 0 || content.charAt(i-1) != '\\') && (i < 2 || content.charAt(i-1) != '`' || content.charAt(i-2) != '\\')) {
                    char next = content.charAt(i+1);

                    if (next == '#') {
                        String hex = content.substring(i+1, i+8);
                        if (hex.matches("#[0-9a-fA-F]{6}")) {
                            message = message.append(building);
                            building = Component.empty().style(building.style()).color(TextColor.fromHexString(hex));
                            i += 7;
                            formatted = true;
                        }
                    } else {
                        ChatFormatting formatting = null;

                        if (next == '`') {
                            String name = content.substring(i + 2);
                            for (Map.Entry<String, ChatFormatting> format : NAMES.entrySet()) {
                                if (name.toLowerCase().startsWith(format.getKey())) {
                                    formatting = format.getValue();
                                    i += format.getKey().length();
                                    break;
                                }
                            }
                        } else {
                            formatting = ChatFormatting.getByCode(Character.toLowerCase(next));
                        }

                        if (formatting != null) {
                            message = message.append(building);
                            building = Component.empty().style(building.style());

                            if (formatting.isColor()) building = building.color(TextColor.color(formatting.getColor()));
                            else if (formatting.isFormat()) building = building.decorate(formatting == ChatFormatting.UNDERLINE ? TextDecoration.UNDERLINED : TextDecoration.valueOf(formatting.name()));
                            else building = building.style(Style.empty());

                            i++;
                            formatted = true;
                        }
                    }
                }

                if (!formatted) {
                    if (i != 0 && c == '`' && content.charAt(i-1) == '\\') {
                        building = building.children(building.children().stream().limit(building.children().size()-1).toList());
                    }

                    building = building.append(Component.text(c));
                }
            }

            event.message(message.append(building));
        }
    }
}
