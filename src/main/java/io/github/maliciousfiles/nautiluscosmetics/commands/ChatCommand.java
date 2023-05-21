package io.github.maliciousfiles.nautiluscosmetics.commands;

import io.github.maliciousfiles.nautiluscosmetics.NautilusCosmetics;
import io.github.maliciousfiles.nautiluscosmetics.cosmetics.Nickname;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ChatCommand implements CommandExecutor, TabCompleter {

    private static Map<UUID, UUID> CHATS = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("You must be a player to use this command").color(NautilusCosmetics.ERROR_COLOR));
            return true;
        }

        if (strings.length == 0 || strings[0].equals("all")) {
            player.sendMessage(Component.text("Now chatting with all players").color(TextColor.color(255, 211, 41)));
            CHATS.remove(player.getUniqueId());
            return true;
        }

        OfflinePlayer chat = Nickname.getPlayerFromNickname(strings[0]);
        if (chat == null) chat = Bukkit.getPlayerExact(strings[0]);

        if (chat == null || !chat.isOnline()) {
            commandSender.sendMessage(Component.text("Player not found").color(NautilusCosmetics.ERROR_COLOR));
            return true;
        }

        player.sendMessage(Component.text("Now chatting with ").append(chat.getPlayer().displayName()).color(TextColor.color(255, 211, 41)));
        CHATS.put(player.getUniqueId(), chat.getUniqueId());

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();
        out.add("all");

        if (strings.length == 1) {
            out.addAll(Bukkit.getOnlinePlayers().stream().map((p) -> NautilusCosmetics.getTextContent(p.displayName())).toList());
        }

        return out.stream().filter(str->str.toLowerCase().startsWith(strings[strings.length-1].toLowerCase())).toList();
    }

    public static class ChatListener implements Listener {

        @EventHandler
        public void onQuit(PlayerQuitEvent e) {
            for (Map.Entry<UUID, UUID> entry : CHATS.entrySet()) {
                if (entry.getValue().equals(e.getPlayer().getUniqueId())) {
                    CHATS.remove(entry.getKey());
                }
            }

            CHATS.remove(e.getPlayer().getUniqueId());
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onChat(AsyncChatEvent e) {
            UUID chat = CHATS.get(e.getPlayer().getUniqueId());
            if (chat != null) {
                e.setCancelled(true);

                Player recipient = Bukkit.getPlayer(chat);
                Bukkit.getScheduler().runTask(NautilusCosmetics.INSTANCE, () -> e.getPlayer().performCommand("msg " + recipient.getName() + " " + NautilusCosmetics.getTextContent(e.originalMessage())));
            }
        }
    }
}
