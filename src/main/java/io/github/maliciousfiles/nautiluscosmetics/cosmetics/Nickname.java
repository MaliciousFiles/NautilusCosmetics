package io.github.maliciousfiles.nautiluscosmetics.cosmetics;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import io.github.maliciousfiles.nautiluscosmetics.NautilusCosmetics;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class Nickname {

    private static final BiMap<UUID, String> playerNames = HashBiMap.create();

    /**
     * Initializes the NicknameListener, and begin the SQL update task.
     */
    public static void init() {
        Bukkit.getPluginManager().registerEvents(new NicknameListener(), NautilusCosmetics.INSTANCE);

        Bukkit.getScheduler().runTaskTimerAsynchronously(NautilusCosmetics.INSTANCE, () -> {
            try {
                Connection connection = NautilusCosmetics.openSql();
                Statement statement = connection.createStatement();

                ResultSet results = statement.executeQuery("SELECT * FROM nicknames");

                playerNames.clear();
                while (results.next()) {
                    playerNames.put(UUID.fromString(results.getString("uuid")), results.getString("nickname"));
                }

                // TODO: make this validate nicknames
                for (Player p : Bukkit.getOnlinePlayers()) {
                    String nickname = playerNames.getOrDefault(p.getUniqueId(), p.getName());

                    if (!NautilusCosmetics.getTextContent(p.displayName()).equals(nickname)) {
                        updateNickname(p, nickname);
                    }
                }
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to load nicknames!", e);
            }
        }, 0, NautilusCosmetics.SQL_UPDATE_TIME*20);
    }

    /**
     * Private helper function to update the map in memory and update the SQL database.
     */
    private static void setNickname(UUID uuid, String nickname) {
        if (nickname == null) playerNames.remove(uuid);
        else playerNames.put(uuid, nickname);

        try {
            Connection connection = NautilusCosmetics.openSql();
            Statement statement = connection.createStatement();

            if (nickname == null) {
                statement.executeUpdate("DELETE FROM nicknames WHERE uuid='" + uuid + "'");
            } else {
                statement.executeUpdate("INSERT INTO nicknames (uuid, nickname) VALUES ('" + uuid + "', '" + nickname + "') ON DUPLICATE KEY UPDATE nickname='" + nickname + "'");
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to set nickname!", e);
        }
    }

    /**
     * Private helper function to actually update a player's nickname in-game.
     */
    private static void updateNickname(Player p, String name) {
        p.displayName(Component.text(name));
        if (NameColor.getNameColor(p) != null) NameColor.setNameColor(p, NameColor.getNameColor(p), false);

        NautilusCosmetics.updateNameTag(p, p.displayName(), Bukkit.getOnlinePlayers());

    }

    /**
     * Get a player's nickname (inverse of #getPlayerFromNickname).
     */
    public static String getNickname(OfflinePlayer p) {
        return playerNames.get(p.getUniqueId());
    }

    /**
     * Get a player by nickname (inverse of #getNickname).
     */
    public static OfflinePlayer getPlayerFromNickname(String nickname) {
        if (!playerNames.containsValue(nickname)) return null;

        String name = Bukkit.getOfflinePlayer(playerNames.inverse().get(nickname)).getName();
        if (name == null) return null;

        return Bukkit.getOfflinePlayerIfCached(name);
    }

    /**
     * Get a list of all nicknames.
     */
    public static List<String> getNicknames() {
        return new ArrayList<>(playerNames.values());
    }

    /**
     * Set a player's nickname
     */
    public static void setNickname(Player p, String name, boolean sendMessage) {
        setNickname(p.getUniqueId(), name.equals(p.getName()) ? null : name);

        if (sendMessage) p.sendMessage(Component.text("Nickname set to ").append(p.displayName()));
        updateNickname(p, name);
    }

    /**
     * Make sure a given nickname is valid based on requirements.
     */
    public static String validateNickname(Player p, String name) {
        if (name.length() > 16) return "That nickname is too long";
        if (name.length() < 3) return "That nickname is too short";
        if (!name.matches("[a-zA-Z0-9_]+") && !p.hasPermission("nautiluscosmetics.nickname.specialchars")) return "Become a supporter to unlock non-alphanumeric characters";
        if ((!name.equals(playerNames.get(p.getUniqueId())) && playerNames.containsValue(name)) || (!name.equals(p.getName()) && Bukkit.getOfflinePlayerIfCached(name) != null)) return "That nickname is already taken";

        return null;
    }

    public static class NicknameListener implements Listener {
        @EventHandler(priority=org.bukkit.event.EventPriority.HIGH)
        public void onPlayerJoin(PlayerJoinEvent e) {
            String nick = getNickname(e.getPlayer());

            if (nick != null) {
                if (Bukkit.getOfflinePlayerIfCached(nick) != null) {
                    setNickname(e.getPlayer(), e.getPlayer().getName(), false);
                    e.getPlayer().sendMessage(Component.text("Your nickname was reset because a player by that name has joined").color(NautilusCosmetics.ERROR_COLOR));
                } else {
                    setNickname(e.getPlayer(), getNickname(e.getPlayer()), false);
                }
            }

            for (Map.Entry<UUID, String> nickEntry : playerNames.entrySet()) {
                Player p = Bukkit.getPlayer(nickEntry.getKey());
                NautilusCosmetics.updateNameTag(p, p.displayName(), List.of(e.getPlayer()));
            }
        }
    }
}
