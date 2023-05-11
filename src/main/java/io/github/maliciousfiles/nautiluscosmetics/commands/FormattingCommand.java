package io.github.maliciousfiles.nautiluscosmetics.commands;

import io.github.maliciousfiles.nautiluscosmetics.NautilusCosmetics;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.ChatFormatting;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FormattingCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!commandSender.hasPermission("nautiluscosmetics.chat.chatcodes")) {
            commandSender.sendMessage(Component.text("Become a sponsor to use chat codes!").color(NautilusCosmetics.ERROR_COLOR));
            return true;
        }

        if (strings.length > 0) {
            if (strings[0].equals("codes")) {
                // TODO: implement
                return true;
            } else if (strings[0].equals("names")) {
                // TODO: implement
                return true;
            }
        }

        commandSender.sendMessage(Component.empty().append(Component.text("Codes ").color(TextColor.color(247, 255, 152)).decorate(TextDecoration.BOLD)).append(Component.text("`x").color(TextColor.color(251, 255, 227))));
        commandSender.sendMessage(Component.text("    Default Minecraft chat code (0-9,a-f,k-o,r)").color(TextColor.color(247, 255, 152)));
        commandSender.sendMessage(Component.text("    `l`cBold and Red → ").color(TextColor.color(247, 255, 152)).append(Component.text("Bold and Red").decorate(TextDecoration.BOLD).color(TextColor.color(ChatFormatting.RED.getColor()))));

        commandSender.sendMessage(Component.empty());

        commandSender.sendMessage(Component.empty().append(Component.text("Names ").decorate(TextDecoration.BOLD).color(TextColor.color(247, 255, 152))).append(Component.text("``name").color(TextColor.color(251, 255, 227))));
        commandSender.sendMessage(Component.text("    Full name of the color or format").color(TextColor.color(247, 255, 152)));
        commandSender.sendMessage(Component.text("    ``bold``redBold and Red → ").color(TextColor.color(247, 255, 152)).append(Component.text("Bold and Red").decorate(TextDecoration.BOLD).color(TextColor.color(ChatFormatting.RED.getColor()))));

        commandSender.sendMessage(Component.empty());

        commandSender.sendMessage(Component.text("/formatting <codes|names> for lists").color(TextColor.color(247, 255, 152)));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();

        if (strings.length == 1) {
            out.add("codes");
            out.add("names");
        }

        return out.stream().filter(s1 -> s1.startsWith(strings[strings.length-1])).toList();
    }
}
