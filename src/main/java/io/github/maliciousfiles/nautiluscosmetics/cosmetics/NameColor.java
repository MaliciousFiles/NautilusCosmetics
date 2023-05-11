package io.github.maliciousfiles.nautiluscosmetics.cosmetics;

import io.github.maliciousfiles.nautiluscosmetics.NautilusCosmetics;
import io.github.maliciousfiles.nautiluscosmetics.util.FancyText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.TextColor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;

public class NameColor {

    private static final Map<UUID, NameColor> playerColors = new HashMap<>();

    public static NameColor getNameColor(OfflinePlayer player) {
        return playerColors.containsKey(player.getUniqueId()) ? playerColors.get(player.getUniqueId()).copy() : null;
    }

    public final FancyText.ColorType type;
    public final TextColor[] colors;

    private NameColor copy() {
        return new NameColor(type, colors);
    }

    private NameColor(FancyText.ColorType type, TextColor... colors) {
        this.type = type;
        this.colors = colors;
    }

    public static void setNameColor(Player player, FancyText.ColorType type, boolean sendMessage, TextColor... colors) {
        NameColor color = new NameColor(type, colors);

        playerColors.put(player.getUniqueId(), color);
        setNameColor(player, color, sendMessage);
    }

    public static void setNameColor(Player player, NameColor color, boolean sendMessage) {
        if (sendMessage) player.sendMessage(FancyText.colorText(color.type, "Name color changed", color.colors));

        player.displayName(FancyText.colorText(color.type, NautilusCosmetics.getTextContent(player.displayName()), color.colors));
        player.playerListName(FancyText.colorText(color.type, NautilusCosmetics.getTextContent(player.playerListName()), color.colors));
    }

    public static class NameColorListener implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent e) {
            if (getNameColor(e.getPlayer()) != null) setNameColor(e.getPlayer(), getNameColor(e.getPlayer()), false);
        }
    }
}
