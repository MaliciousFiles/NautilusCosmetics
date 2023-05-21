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

public class MsgtoggleCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            return false;
        }
        Player player = (Player) commandSender;

        Player receiver = null;
        for (Player online : NautilusCosmetics.INSTANCE.getServer().getOnlinePlayers()) {
            if (strings[0].equalsIgnoreCase(online.getName()) || strings[0].equalsIgnoreCase(online.getDisplayName())) {
                receiver = online;
            }
        }
        if (receiver == null) {
            player.sendMessage(Component.text("Please provide a valid player name").color(NautilusCosmetics.ERROR_COLOR));
            player.sendMessage(getUsageMessage());
            return false;
        }
        MsgManager.toggle(player, receiver);
        player.sendMessage(Component.text("Toggled messaging to " + strings[0]).color(NautilusCosmetics.CONSOLE_COLOR));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        ArrayList<String> args = new ArrayList<>();
        if (strings.length == 1) {
            ArrayList<Player> players = new ArrayList<>(NautilusCosmetics.INSTANCE.getServer().getOnlinePlayers());
            for (int i = 0; i < players.size(); i++) {
                args.add(players.get(i).getDisplayName());
            }
        }
        return args;
    }
    public Component getUsageMessage() {
        String text = "/msgtoggle <player>";
        return Component.text(text).color(NautilusCosmetics.CONSOLE_COLOR);
    }
}
