package io.github.maliciousfiles.nautiluscosmetics.commands;

import io.github.maliciousfiles.nautiluscosmetics.NautilusCosmetics;
import io.github.maliciousfiles.nautiluscosmetics.cosmetics.MessageStyler;
import io.github.maliciousfiles.nautiluscosmetics.util.FancyText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CustomMsgCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length < 2) return false;

        Player recipient = Bukkit.getPlayerExact(strings[0]);
        if (recipient == null) {
            commandSender.sendMessage(Component.text("Player not found").color(NautilusCosmetics.ERROR_COLOR));
            return true;
        }

        String message = s.split(strings[0]+" ")[1];

        recipient.sendMessage(Component.text("   ")
                .append(MessageStyler.getTimeStamp())
                .append(commandSender instanceof Player p ? p.displayName() : commandSender.name())
                .append(Component.text(" whispered to you").color(TextColor.color(100, 100, 100)).decorate(TextDecoration.ITALIC))
                .append(Component.text(" » ").color(TextColor.color(150, 150, 150)))
                .append((commandSender.hasPermission(NautilusCosmetics.CHAT_FORMATTING_PERM) ?
                        FancyText.parseChatFormatting(message) :
                        Component.text(message)).color(NautilusCosmetics.DEFAULT_TEXT_COLOR))
        );

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();

        if (strings.length == 1) {
            out.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
        }

        return out.stream().filter(str->str.toLowerCase().startsWith(strings[strings.length-1].toLowerCase())).toList();
    }
}
