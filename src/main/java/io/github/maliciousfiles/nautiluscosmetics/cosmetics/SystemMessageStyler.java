package io.github.maliciousfiles.nautiluscosmetics.cosmetics;

import io.github.maliciousfiles.nautiluscosmetics.NautilusCosmetics;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SystemMessageStyler implements Listener {

    private static TranslatableComponent styleMessage(TranslatableComponent message) {
        List<Component> args = new ArrayList<>(message.args());

        for (int i = 0; i < args.size(); i++) {
            TextComponent component = (TextComponent) args.get(i);
            Player player = Bukkit.getPlayerExact(component.content());

            if (player != null) {
                args.set(i, player.displayName()
                        .clickEvent(component.clickEvent())
                        .hoverEvent(component.hoverEvent()));
            }
        }

        return message.args(args);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        e.deathMessage(styleMessage((TranslatableComponent) e.deathMessage()));
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        e.joinMessage(styleMessage((TranslatableComponent) e.joinMessage()));
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        e.quitMessage(styleMessage((TranslatableComponent) e.quitMessage()));
    }

    @EventHandler
    public void onMessage(PlayerChatEvent e) {
        e.setCancelled(true);
        TextComponent textComponent = Component.text("")
                .append(e.getPlayer().displayName())
                .append(Component.text(ChatColor.GRAY + " » " + ChatColor.WHITE + e.getMessage()));
        for(Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(textComponent);
        }
        Bukkit.getLogger().info(e.getPlayer().getName() + " » " + e.getMessage());
    }

}