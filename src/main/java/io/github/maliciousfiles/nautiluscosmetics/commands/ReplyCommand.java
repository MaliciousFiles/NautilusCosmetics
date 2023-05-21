package io.github.maliciousfiles.nautiluscosmetics.commands;

import io.github.maliciousfiles.nautiluscosmetics.NautilusCosmetics;
import io.github.maliciousfiles.nautiluscosmetics.util.MsgManager;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ReplyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            return false;
        }
        Player player = (Player) commandSender;
        if (!MsgManager.hasLastMessager(player)) {
            player.sendMessage(Component.text("No messages to reply to").color(NautilusCosmetics.ERROR_COLOR));
            player.sendMessage(getUsageMessage());
        }
        Player receiver = MsgManager.getLastMessager(player);
        assert receiver != null;
        if (!receiver.isOnline()) {
            player.sendMessage(Component.text("Player no longer online").color(NautilusCosmetics.ERROR_COLOR));
        }
        String str = NautilusCosmetics.argsToString(strings, 0);
        if (str.equals("")) {
            player.sendMessage(Component.text("Please provide a message to send").color(NautilusCosmetics.ERROR_COLOR));
            player.sendMessage(getUsageMessage());
            return false;
        }
        receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', str)); //replace with custom colors???
        MsgManager.updateLastMessager(receiver, player);
        return true;
    }

    public Component getUsageMessage() {
        String text = "/reply <message>";
        return Component.text(text).color(NautilusCosmetics.CONSOLE_COLOR);
    }
}
