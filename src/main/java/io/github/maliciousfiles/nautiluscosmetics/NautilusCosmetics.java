package io.github.maliciousfiles.nautiluscosmetics;

import io.github.maliciousfiles.nautiluscosmetics.commands.CosmeticsCommand;
import io.github.maliciousfiles.nautiluscosmetics.commands.FormattingCommand;
import io.github.maliciousfiles.nautiluscosmetics.commands.NicknameCommand;
import io.github.maliciousfiles.nautiluscosmetics.cosmetics.SponsorChatEffects;
import io.github.maliciousfiles.nautiluscosmetics.cosmetics.SystemMessageStyler;
import io.github.maliciousfiles.nautiluscosmetics.cosmetics.NameColor;
import io.github.maliciousfiles.nautiluscosmetics.cosmetics.Nickname;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

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

    public static String getTextContent(Component component) {
        String out = "";

        if (component instanceof TextComponent text) out += text.content();
        for (Component child : component.children()) out += getTextContent(child);

        return out;
    }
}
