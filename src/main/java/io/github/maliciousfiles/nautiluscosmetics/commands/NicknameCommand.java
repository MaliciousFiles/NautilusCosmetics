package io.github.maliciousfiles.nautiluscosmetics.commands;

import io.github.maliciousfiles.nautiluscosmetics.NautilusCosmetics;
import io.github.maliciousfiles.nautiluscosmetics.cosmetics.Nickname;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NicknameCommand implements CommandExecutor, TabCompleter {

    private static final int PAGE_SIZE = 5;
    private static final TextColor COLOR = TextColor.color(255, 185, 21);

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!commandSender.hasPermission("nautiluscosmetics.nickname.view")) {
            commandSender.sendMessage(Component.text("You do not have permission to use this command").color(NautilusCosmetics.ERROR_COLOR));
            return true;
        }

        if (strings.length < 1) return false;

        Consumer<OfflinePlayer> sendNickname = p -> {
            String nick = Nickname.getNickname(p);
            commandSender.sendMessage(Component.text(p.getName()).append(Component.text(" → ").color(COLOR)).append(p instanceof Player pl ? pl.displayName() : Component.text(nick != null ? nick : p.getName())));
        };

        switch (strings[0]) {
            case "list"->{
                int pageMax = (int) Math.ceil((double) Bukkit.getOnlinePlayers().size()/PAGE_SIZE);
                int page = Math.min(strings.length > 1 ? Integer.parseInt(strings[1]) : 1, pageMax);

                commandSender.sendMessage(Component.text("----- Nicknames (Page "+page+"/"+pageMax+") -----").color(COLOR).decorate(TextDecoration.BOLD));

                for (int i = 0; i < PAGE_SIZE; i++) {
                    int idx = (page-1)*PAGE_SIZE+i;
                    if (idx >= Bukkit.getOnlinePlayers().size()) break;

                    Player p = Bukkit.getOnlinePlayers().stream().sorted().toList().get(idx);
                    commandSender.sendMessage(Component.empty().append(Component.text(" - ").color(COLOR)).append(p.name()).append(Component.text(" → ").color(COLOR)).append(p.displayName()));
                }
            }
            case "nickname"->{
                if (strings.length < 2) return false;

                OfflinePlayer p = Nickname.getPlayerFromNickname(strings[1]);
                if (p == null || p.getName() == null) {
                    commandSender.sendMessage(Component.text("Nickname not found").color(NautilusCosmetics.ERROR_COLOR));
                    return true;
                } else {
                    sendNickname.accept(p);
                }
            }
            case "player"->{
                if (strings.length < 2) return false;

                OfflinePlayer p = Bukkit.getOfflinePlayerIfCached(strings[1]);
                if (p == null || p.getName() == null) {
                    commandSender.sendMessage(Component.text("Player not found").color(NautilusCosmetics.ERROR_COLOR));
                    return true;
                } else {
                    sendNickname.accept(p);
                }
            }
            default->{
                return false;
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();

        if (strings.length == 1) {
            out.add("list");
            out.add("player");
            out.add("nickname");
        } else if (strings.length == 2) {
            if (strings[0].equals("player")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    out.add(p.getName());
                }
            } else if (strings[0].equals("nickname")) {
                for (String n : Nickname.getNicknames()) {
                    out.add(n);
                }
            }
        }

        return out.stream().filter(str->str.startsWith(strings[strings.length-1])).toList();
    }
}
