package io.github.maliciousfiles.nautiluscosmetics;

import com.mojang.authlib.GameProfile;
import io.github.maliciousfiles.nautiluscosmetics.commands.CosmeticsCommand;
import io.github.maliciousfiles.nautiluscosmetics.commands.FormattingCommand;
import io.github.maliciousfiles.nautiluscosmetics.commands.NicknameCommand;
import io.github.maliciousfiles.nautiluscosmetics.cosmetics.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.Optionull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public final class NautilusCosmetics extends JavaPlugin {

    public static NautilusCosmetics INSTANCE;
    public static final TextColor ERROR_COLOR = TextColor.color(255, 42, 52);

    @Override
    public void onEnable() {
        INSTANCE = this;

        this.getCommand("cosmetics").setExecutor(new CosmeticsCommand());
        this.getCommand("nickname").setExecutor(new NicknameCommand());
        this.getCommand("formatting").setExecutor(new FormattingCommand());

        Bukkit.getPluginManager().registerEvents(new SystemMessageStyler(), this);
        Bukkit.getPluginManager().registerEvents(new NameColor.NameColorListener(), this);
        Bukkit.getPluginManager().registerEvents(new Nickname.NicknameListener(), this);
        Bukkit.getPluginManager().registerEvents(new SponsorChatEffects(), this);
    }

    public static void setNameTag(Player player, Component name) {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();

        nmsPlayer.connection.send(new ClientboundPlayerInfoRemovePacket(List.of(player.getUniqueId())));

        GameProfile profile = new GameProfile(player.getUniqueId(), getTextContent(name));
        profile.getProperties().putAll(nmsPlayer.gameProfile.getProperties());

        FriendlyByteBuf buf = new FriendlyByteBuf(ByteBufAllocator.DEFAULT.buffer());
        buf.writeEnumSet(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER), ClientboundPlayerInfoUpdatePacket.Action.class);
        buf.writeCollection(List.of(new ClientboundPlayerInfoUpdatePacket.Entry(player.getUniqueId(), profile, true, nmsPlayer.latency, nmsPlayer.gameMode.getGameModeForPlayer(), nmsPlayer.getTabListDisplayName(), Optionull.map(nmsPlayer.getChatSession(), RemoteChatSession::asData))),
                (buf2, entry) -> {
                    buf2.writeUUID(entry.profileId());

                    buf2.writeUtf(entry.profile().getName(), 16);
                    buf2.writeGameProfileProperties(entry.profile().getProperties());
                });
        nmsPlayer.connection.send(new ClientboundPlayerInfoUpdatePacket(buf));


        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p == player) continue;

            ServerPlayer nms = ((CraftPlayer) p).getHandle();

            buf = new FriendlyByteBuf(ByteBufAllocator.DEFAULT.buffer());
            buf.writeEnumSet(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER), ClientboundPlayerInfoUpdatePacket.Action.class);
            buf.writeCollection(List.of(new ClientboundPlayerInfoUpdatePacket.Entry(player.getUniqueId(), profile, true, nmsPlayer.latency, nmsPlayer.gameMode.getGameModeForPlayer(), nmsPlayer.getTabListDisplayName(), Optionull.map(nmsPlayer.getChatSession(), RemoteChatSession::asData))),
                    (buf2, entry) -> {
                        buf2.writeUUID(entry.profileId());

                        buf2.writeUtf(entry.profile().getName(), 16);
                        buf2.writeGameProfileProperties(entry.profile().getProperties());
                    });
            nms.connection.send(new ClientboundRemoveEntitiesPacket(player.getEntityId()));
            nmsPlayer.connection.send(new ClientboundPlayerInfoUpdatePacket(buf));
            nms.connection.send(new ClientboundAddPlayerPacket(((CraftPlayer) player).getHandle()));
        }
    }

    public static String getTextContent(Component component) {
        String out = "";

        if (component instanceof TextComponent text) out += text.content();
        for (Component child : component.children()) out += getTextContent(child);

        return out;
    }
}
