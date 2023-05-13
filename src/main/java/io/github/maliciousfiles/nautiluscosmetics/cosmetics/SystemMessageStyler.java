package io.github.maliciousfiles.nautiluscosmetics.cosmetics;

import io.github.maliciousfiles.nautiluscosmetics.NautilusCosmetics;
import net.kyori.adventure.text.Component;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class SystemMessageStyler implements Listener {

    public ArrayList<TextComponent> runningMessages = new ArrayList<>();

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
        //e.deathMessage(styleMessage((TranslatableComponent) e.deathMessage()));
        String message = c("&b&lDeath &r&b☠ &8» &7" + e.getDeathMessage());
        e.setDeathMessage(message);
        Bukkit.getScheduler().scheduleSyncDelayedTask(NautilusCosmetics.INSTANCE, new Runnable() {
            @Override
            public void run() {
                e.getPlayer().sendMessage(c("&7Your coordinates: (" + Math.round(e.getPlayer().getLocation().getX()) + ", " +
                        Math.round(e.getPlayer().getLocation().getZ()) + ")"));
            }
        }, 10);
        addToRunning(Component.text(message));
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        //e.joinMessage(styleMessage((TranslatableComponent) e.joinMessage()));
        e.setJoinMessage(c("&aJoin &8| &f" + e.getPlayer().getName()));
        TextComponent tc = Component.text(ChatColor.GREEN + "Join " + ChatColor.DARK_GRAY + "| " + ChatColor.WHITE)
                .append(e.getPlayer().displayName());
        addToRunning(tc);
    }

    @EventHandler
    public void onJoinRunning(PlayerJoinEvent e) {
        for(TextComponent c : runningMessages) {
            e.getPlayer().sendMessage(c);
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(NautilusCosmetics.INSTANCE, new Runnable() {
            @Override
            public void run() {
                e.getPlayer().sendMessage(c("&b&kx &r&7Feel welcome at &3Nautilus&b&lMC&r&7, " + e.getPlayer().getName() + "! &b&kx"));
            }
        }, 10);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        //e.quitMessage(styleMessage((TranslatableComponent) e.quitMessage()));
        boolean to = e.getReason().equals(PlayerQuitEvent.QuitReason.TIMED_OUT);
        e.setQuitMessage(c("&4Leave &8| &f" + e.getPlayer().getName() + (to ? " &f(timed out)" : "")));
        TextComponent tc = Component.text(ChatColor.DARK_RED + "Leave " + ChatColor.DARK_GRAY + "| " + ChatColor.WHITE)
                .append(e.getPlayer().displayName());
        addToRunning(tc);
    }

    @EventHandler
    public void onMessage(PlayerChatEvent e) {
        e.setCancelled(true);
        TextComponent textComponent = Component.text(timestamp() + " ")
                .append(e.getPlayer().displayName())
                .append(Component.text(ChatColor.DARK_GRAY + " » " + ChatColor.WHITE + e.getMessage()));
        for(Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(textComponent);
        }
        Bukkit.getLogger().info(e.getPlayer().getName() + " » " + e.getMessage());

        addToRunning(textComponent);
    }

    private String timestamp() {
        Calendar c = GregorianCalendar.getInstance();
        int m = c.get(Calendar.MINUTE);
        int s = c.get(Calendar.SECOND);
        return ChatColor.DARK_AQUA + "" + (m < 10 ? "0" : "") + m + ":" + (s < 10 ? "0" : "") + s;
    }

    private void addToRunning(TextComponent c) {
        runningMessages.add(c);
        if(runningMessages.size() > 50) runningMessages.remove(0);
    }

    public static String c(String s){
        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', s);
    }

}