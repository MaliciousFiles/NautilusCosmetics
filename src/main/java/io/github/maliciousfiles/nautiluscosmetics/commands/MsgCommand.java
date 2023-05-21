package io.github.maliciousfiles.nautiluscosmetics.commands;

import io.github.maliciousfiles.nautiluscosmetics.NautilusCosmetics;
import io.github.maliciousfiles.nautiluscosmetics.cosmetics.MessageStyler;
import io.github.maliciousfiles.nautiluscosmetics.cosmetics.Nickname;
import io.github.maliciousfiles.nautiluscosmetics.util.FancyText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MsgCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length < 2) return false;

        OfflinePlayer recipient = Nickname.getPlayerFromNickname(strings[0]);
        if (recipient == null) recipient = Bukkit.getPlayerExact(strings[0]);

        if (recipient == null || !recipient.isOnline()) {
            commandSender.sendMessage(Component.text("Player not found").color(NautilusCosmetics.ERROR_COLOR));
            return true;
        }

        String message = String.join(" ", Arrays.copyOfRange(strings, 1, strings.length));

        Component msg = (commandSender.hasPermission(NautilusCosmetics.CHAT_FORMATTING_PERM) ?
                FancyText.parseChatFormatting(message) :
                Component.text(message)).color(NautilusCosmetics.DEFAULT_TEXT_COLOR);

        if (commandSender instanceof Player player) {
            ReplyCommand.messaged(player.getUniqueId(), recipient.getUniqueId());
        }

        recipient.getPlayer().sendMessage(Component.empty()
                .append(MessageStyler.getTimeStamp())
                .append(modifyColor((commandSender instanceof Player p ? p.displayName() : commandSender.name()).decorate(TextDecoration.ITALIC), -30, -30, -30))
                .append(Component.text(" whispered to you").color(TextColor.color(150, 150, 150)).decorate(TextDecoration.ITALIC))
                .append(Component.text(" » ").color(TextColor.color(150, 150, 150)))
                .append(msg)
        );
        commandSender.sendMessage(Component.empty()
                .append(MessageStyler.getTimeStamp())
                .append(Component.text("You whispered to ").color(TextColor.color(150, 150, 150)).decorate(TextDecoration.ITALIC))
                .append(modifyColor(recipient.getPlayer().displayName(), -30, -30, -30).decorate(TextDecoration.ITALIC))
                .append(Component.text(" » ").color(TextColor.color(150, 150, 150)))
                .append(msg)
        );

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();

        if (strings.length == 1) {
            out.addAll(Bukkit.getOnlinePlayers().stream().map((p) -> NautilusCosmetics.getTextContent(p.displayName())).toList());
        }

        return out.stream().filter(str->str.toLowerCase().startsWith(strings[strings.length-1].toLowerCase())).toList();
    }

    public Component modifyColor(Component component, int rMod, int gMod, int bMod) {
        List<Component> children = new ArrayList<>(component.children());
        children.replaceAll(c -> modifyColor(c, rMod, gMod, bMod));

        TextColor color = component.color() == null ? TextColor.color(255, 255, 255) : component.color();
        return component.children(children).color(TextColor.color(color.red()+rMod, color.green()+gMod, color.blue()+bMod));
    }
}
