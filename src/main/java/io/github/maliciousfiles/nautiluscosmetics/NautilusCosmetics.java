package io.github.maliciousfiles.nautiluscosmetics;

import com.mojang.authlib.GameProfile;
import io.github.maliciousfiles.nautiluscosmetics.commands.CosmeticsCommand;
import io.github.maliciousfiles.nautiluscosmetics.commands.FormattingCommand;
import io.github.maliciousfiles.nautiluscosmetics.commands.NicknameCommand;
import io.github.maliciousfiles.nautiluscosmetics.cosmetics.*;
import io.github.maliciousfiles.nautiluscosmetics.util.FancyText;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class NautilusCosmetics extends JavaPlugin {

    public static NautilusCosmetics INSTANCE;
    public static final TextColor ERROR_COLOR = TextColor.color(255, 42, 52);

    private static final List<String> existingNames = new ArrayList<>();

    @Override
    public void onEnable() {
        INSTANCE = this;

        this.getCommand("cosmetics").setExecutor(new CosmeticsCommand());
        this.getCommand("nickname").setExecutor(new NicknameCommand());
        this.getCommand("formatting").setExecutor(new FormattingCommand());

        Bukkit.getPluginManager().registerEvents(new MessageStyler(), this);
        Bukkit.getPluginManager().registerEvents(new NameColor.NameColorListener(), this);
        Bukkit.getPluginManager().registerEvents(new Nickname.NicknameListener(), this);
        Bukkit.getPluginManager().registerEvents(new SponsorChatEffects(), this);
    }

    public static void setNameTagName(Player player, String name, Collection<? extends Player> players) {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        GameProfile oldProfile = nmsPlayer.gameProfile;
        nmsPlayer.gameProfile = new GameProfile(player.getUniqueId(), name);
        nmsPlayer.gameProfile.getProperties().putAll(oldProfile.getProperties());

        ClientboundPlayerInfoRemovePacket remove = new ClientboundPlayerInfoRemovePacket(List.of(player.getUniqueId()));
        ClientboundPlayerInfoUpdatePacket update = ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(nmsPlayer));
        nmsPlayer.gameProfile = oldProfile;

        for (Player p : players) {
            if (p == player) continue;
            ServerPlayer nms = ((CraftPlayer) p).getHandle();

            nms.connection.send(remove);
            nms.connection.send(update);

            if (nmsPlayer.tracker != null) {
                nmsPlayer.tracker.serverEntity.removePairing(nms);
                nmsPlayer.tracker.serverEntity.addPairing(nms);
            }
        }
    }

    public static void updateNameTag(Player player, Component name, Collection<? extends Player> players) {
        String text = "%08x".formatted(player.getEntityId()).replaceAll("(.)", "§$1");

        existingNames.add(text);

        setNameTagName(player, text, players);

        PlayerTeam team = new PlayerTeam(new Scoreboard(), player.getName());
        team.setPlayerPrefix(PaperAdventure.asVanilla(name));

        ClientboundSetPlayerTeamPacket addTeam = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
        ClientboundSetPlayerTeamPacket addPlayer = ClientboundSetPlayerTeamPacket.createPlayerPacket(team, text, ClientboundSetPlayerTeamPacket.Action.ADD);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p == player) continue;
            ServerPlayer nms = ((CraftPlayer) p).getHandle();

            nms.connection.send(addTeam);
            nms.connection.send(addPlayer);
        }
    }

    public static String getTextContent(Component component) {
        String out = "";

        if (component instanceof TextComponent text) out += text.content();
        for (Component child : component.children()) out += getTextContent(child);

        return out;
    }

    public static Component format(Component component, ChatFormatting formatting) {
        if (formatting.isColor()) return component.color(PaperAdventure.asAdventure(formatting));
        else if (formatting.isFormat()) return component.decorate(formatting == ChatFormatting.UNDERLINE ? TextDecoration.UNDERLINED : TextDecoration.valueOf(formatting.name()));
        else return component;
    }
}
