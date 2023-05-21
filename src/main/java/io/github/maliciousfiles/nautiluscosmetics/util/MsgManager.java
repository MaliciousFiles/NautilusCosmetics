package io.github.maliciousfiles.nautiluscosmetics.util;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;

import java.util.HashMap;
import java.util.UUID;

public class MsgManager implements Listener {
    private static HashMap<UUID, UUID> lastMessager;
    private static HashMap<UUID, UUID> msgToggle;

    public static Boolean hasLastMessager(Player p) {
        return lastMessager.containsKey(p.getUniqueId());
    }
    public static Player getLastMessager(Player p) {
        if (!hasLastMessager(p)) {
            return null;
        }
        return Bukkit.getPlayer(lastMessager.get(p.getUniqueId()));
    }
    public static boolean isToggled(Player p) {
        return msgToggle.containsKey(p.getUniqueId());
    }
    public static Player getToggledPlayer(Player p) {
        if (!isToggled(p)) {
            return null;
        }
        return Bukkit.getPlayer(msgToggle.get(p.getUniqueId()));
    }
    public static void toggle(Player p, Player receiver) {
        if (!isToggled(p)) {
            msgToggle.put(p.getUniqueId(), receiver.getUniqueId());
        }
        else {
            msgToggle.remove(p.getUniqueId());
        }
    }

    public static void updateLastMessager(Player p, Player messager) {
        if (!hasLastMessager(p)) {
            lastMessager.put(p.getUniqueId(), messager.getUniqueId());
        }
        else {
            lastMessager.replace(p.getUniqueId(), messager.getUniqueId());
        }
    }

    @EventHandler
    public static void onMsgtoggleChat(AsyncChatEvent event) {
        if (!isToggled(event.getPlayer())) {
            return;
        }
        event.setCancelled(true);
        String message = event.originalMessage().toString();

        getToggledPlayer(event.getPlayer()).sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
