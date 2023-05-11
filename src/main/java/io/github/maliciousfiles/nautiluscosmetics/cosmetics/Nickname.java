package io.github.maliciousfiles.nautiluscosmetics.cosmetics;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import io.github.maliciousfiles.nautiluscosmetics.NautilusCosmetics;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;

public class Nickname {

    private static final BiMap<UUID, Component> playerNames = HashBiMap.create();

    public static Component getPlayerName(OfflinePlayer p) {
        return playerNames.get(p.getUniqueId());
    }
    public static OfflinePlayer getPlayerFromNickname(String nickname) {
        if (!playerNames.containsValue(Component.text(nickname))) return null;

        String name = Bukkit.getOfflinePlayer(playerNames.inverse().get(Component.text(nickname))).getName();
        if (name == null) return null;

        return Bukkit.getOfflinePlayerIfCached(name);
    }
    public static List<Component> getNicknames() {
        return new ArrayList<>(playerNames.values());
    }

    public static void setNickname(Player p, Component name, boolean sendMessage) {
        p.displayName(name);
        p.playerListName(name);

        if (NameColor.getNameColor(p) != null) NameColor.setNameColor(p, NameColor.getNameColor(p), false);

        if (sendMessage) p.sendMessage(Component.text("Nickname set to ").append(p.displayName()));

       playerNames.put(p.getUniqueId(), name);
    }

    public static String validateNickname(Player p, Component name) {
        boolean hasPerk = p.hasPermission("nautiluscosmetics.nickname.specialchars");
        String content = NautilusCosmetics.getTextContent(name);

        if (content.length() > 16) return "That nickname is too long";
        if (content.length() < 3) return "That nickname is too short";
        if (!content.matches("[a-zA-Z0-9_]+") && !hasPerk) return "Become a supporter to unlock non-alphanumeric characters";
        if (playerNames.containsValue(name) || (!content.equals(p.getName()) && Bukkit.getOfflinePlayerIfCached(content) != null)) return "That nickname is already taken";

        return null;
    }

    public static class NicknameListener implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent e) {
            Component nick = getPlayerName(e.getPlayer());

            if (nick != null) {
                if (Bukkit.getOfflinePlayerIfCached(NautilusCosmetics.getTextContent(nick)) != null) {
                    setNickname(e.getPlayer(), e.getPlayer().name(), false);
                    e.getPlayer().sendMessage(Component.text("Your nickname was reset because a player by that name has joined").color(NautilusCosmetics.ERROR_COLOR));
                } else {
                    setNickname(e.getPlayer(), getPlayerName(e.getPlayer()), false);
                }
            }
        }
    }
}
