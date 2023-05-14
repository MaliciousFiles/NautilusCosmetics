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

    private static final BiMap<UUID, String> playerNames = HashBiMap.create();

    public static String getPlayerName(OfflinePlayer p) {
        return playerNames.get(p.getUniqueId());
    }
    public static OfflinePlayer getPlayerFromNickname(String nickname) {
        if (!playerNames.containsValue(nickname)) return null;

        String name = Bukkit.getOfflinePlayer(playerNames.inverse().get(nickname)).getName();
        if (name == null) return null;

        return Bukkit.getOfflinePlayerIfCached(name);
    }
    public static List<String> getNicknames() {
        return new ArrayList<>(playerNames.values());
    }

    public static void setNickname(Player p, String name, boolean sendMessage) {
        if (name.equals(p.getName())) {
            playerNames.remove(p.getUniqueId());
        } else {
            playerNames.put(p.getUniqueId(), name);
        }

        p.displayName(Component.text(name));
        NautilusCosmetics.setNameTag(p, Component.text(name));

        if (NameColor.getNameColor(p) != null) NameColor.setNameColor(p, NameColor.getNameColor(p), false);

        if (sendMessage) p.sendMessage(Component.text("Nickname set to ").append(p.displayName()));

    }

    public static String validateNickname(Player p, String name) {
        boolean hasPerk = p.hasPermission("nautiluscosmetics.nickname.specialchars");

        if (name.length() > 16) return "That nickname is too long";
        if (name.length() < 3) return "That nickname is too short";
        if (!name.matches("[a-zA-Z0-9_]+") && !hasPerk) return "Become a supporter to unlock non-alphanumeric characters";
        if ((!name.equals(playerNames.get(p.getUniqueId())) && playerNames.containsValue(name)) || (!name.equals(p.getName()) && Bukkit.getOfflinePlayerIfCached(name) != null)) return "That nickname is already taken";

        return null;
    }

    public static class NicknameListener implements Listener {
        @EventHandler(priority=org.bukkit.event.EventPriority.HIGHEST)
        public void onPlayerJoin(PlayerJoinEvent e) {
            String nick = getPlayerName(e.getPlayer());

            if (nick != null) {
                if (Bukkit.getOfflinePlayerIfCached(nick) != null) {
                    setNickname(e.getPlayer(), e.getPlayer().getName(), false);
                    e.getPlayer().sendMessage(Component.text("Your nickname was reset because a player by that name has joined").color(NautilusCosmetics.ERROR_COLOR));
                } else {
                    setNickname(e.getPlayer(), getPlayerName(e.getPlayer()), false);
                }
            }
        }
    }
}
