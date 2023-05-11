package io.github.maliciousfiles.nautiluscosmetics.commands;

import com.google.common.collect.ImmutableMap;
import io.github.maliciousfiles.nautiluscosmetics.NautilusCosmetics;
import io.github.maliciousfiles.nautiluscosmetics.cosmetics.NameColor;
import io.github.maliciousfiles.nautiluscosmetics.cosmetics.Nickname;
import io.github.maliciousfiles.nautiluscosmetics.util.FancyText;
import io.github.maliciousfiles.nautilusgui.Gui;
import io.github.maliciousfiles.nautilusgui.components.BackGuiComponent;
import io.github.maliciousfiles.nautilusgui.components.ButtonGuiComponent;
import io.github.maliciousfiles.nautilusgui.components.GuiComponent;
import io.github.maliciousfiles.nautilusgui.components.OpenPageGuiComponent;
import io.github.maliciousfiles.nautilusgui.page.BasicGuiPage;
import io.github.maliciousfiles.nautilusgui.page.GuiPage;
import io.github.maliciousfiles.nautilusgui.page.TextInputGuiPage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Items;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class CosmeticsCommand implements CommandExecutor, TabCompleter {

    private static final Map<String, Integer> SUBCOMMANDS = ImmutableMap.of(
            "color", 2,
            "nickname", 2
    ); // subcommand name to number of arguments (for setting)

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length < 1) {
            commandSender.sendMessage(getUsageMessage(strings));
            return true;
        };

        switch (strings[0]) {
            case "menu" -> CosmeticsMenu.openMenu(commandSender, command, s, strings);
            case "set" -> setCosmetic(commandSender, command, s, strings);
            case "clear" -> clearCosmetic(commandSender, command, s, strings);
            default -> commandSender.sendMessage(getUsageMessage(strings));
        }

        return true;
    }

    private Component getUsageMessage(String[] args) {
        String out = "Usage: /cosmetics ";

        if (args.length < 1) {
            out += "<menu|set|clear> [args]";
        } else {
            switch (args[0]) {
                case "set" -> {
                    if (args.length < 2) {
                        out += "set <";
                        for (int i = 0; i < SUBCOMMANDS.size(); i++) {
                            out += SUBCOMMANDS.keySet().stream().toList().get(i);
                            if (i != SUBCOMMANDS.size() - 1) out += "|";
                        }
                        out += "> [args]";
                    } else {
                        switch (args[1]) {
                            case "color" -> {
                                out += "set color <";
                                for (FancyText.ColorType type : FancyText.ColorType.values()) {
                                    out += type.name().toLowerCase()+"|";
                                }
                                out = out.substring(0, out.length()-1)+"> <color> [color 2]";
                            }
                            case "nickname" -> out += "set nickname <nickname>";
                        }
                    }
                }
                case "menu" -> out += "menu";
                case "clear" -> {
                    out += "clear <";
                    for (int i = 0; i < SUBCOMMANDS.size(); i++) {
                        out += SUBCOMMANDS.keySet().stream().toList().get(i);
                        if (i != SUBCOMMANDS.size() - 1) out += "|";
                    }
                    out += ">";
                }
                default -> out += "<menu|set|clear> [args]";
            }
        }


        return Component.text(out).color(TextColor.color(186, 186, 186));
    }

    private void setCosmetic(CommandSender sender, Command command, String s, String[] strings) {
        if (strings.length < 2) {
            sender.sendMessage(getUsageMessage(strings));
            return;
        };

        Player player = null;

        int playerIdx = SUBCOMMANDS.containsKey(strings[1]) ? SUBCOMMANDS.get(strings[1])+3 : -1;

        Optional<FancyText.ColorType> colorType = Optional.empty();
        if (playerIdx != -1 && strings.length > 2 && strings[1].equals("color")) {
            if (!(colorType = Arrays.stream(FancyText.ColorType.values()).filter(t->t.name().equalsIgnoreCase(strings[2])).findFirst()).isPresent()) {
                sender.sendMessage(getUsageMessage(strings));
                return;
            }
            playerIdx += colorType.get().numColors-1;
        }

        if (playerIdx!= -1 && strings.length >= playerIdx) {
            if (!sender.hasPermission("nautiluscosmetics.set.other")) {
                sender.sendMessage(Component.text("Not enough permissions").style(Style.style(NautilusCosmetics.ERROR_COLOR)));
                return;
            }

            if ((player = Bukkit.getPlayerExact(strings[playerIdx-1])) == null) {
                sender.sendMessage(Component.text("Player not found").style(Style.style(NautilusCosmetics.ERROR_COLOR)));
                return;
            }
        }

        if (player == null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("You must be or specify a player to run this command").style(Style.style(NautilusCosmetics.ERROR_COLOR)));
                return;
            }
            player = (Player) sender;
        }

        if (strings[1].equals("color")) {
            if (colorType.isEmpty() || strings.length < 3+colorType.get().numColors) {
                sender.sendMessage(getUsageMessage(strings));
                return;
            }

            net.minecraft.network.chat.TextColor colors[] = new net.minecraft.network.chat.TextColor[colorType.get().numColors];
            for (int i = 0; i < colors.length; i++) {
                colors[i] = net.minecraft.network.chat.TextColor.parseColor(strings[3+i].toLowerCase().replace(' ', '_'));
            }

            if (Arrays.stream(colors).anyMatch(Objects::isNull)) {
                sender.sendMessage(Component.text("Invalid color").style(Style.style(NautilusCosmetics.ERROR_COLOR)));
                return;
            }

            NameColor.setNameColor(player, colorType.get(), true, Arrays.stream(colors).map(c->TextColor.color(c.getValue())).toArray(TextColor[]::new));

            return;
        } else if (strings[1].equals("nickname")) {
            if (strings.length < 3) {
                sender.sendMessage(getUsageMessage(strings));
                return;
            }

            String error = Nickname.validateNickname((Player) sender, Component.text(strings[2]));
            if (error != null) {
                sender.sendMessage(Component.text(error).style(Style.style(NautilusCosmetics.ERROR_COLOR)));
                return;
            }

            Nickname.setNickname(player, Component.text(strings[2]), true);

            return;
        }

        sender.sendMessage(getUsageMessage(strings));
    }

    private void clearCosmetic(CommandSender sender, Command command, String s, String[] strings) {
        if (strings.length < 2) {
            sender.sendMessage(getUsageMessage(strings));
            return;
        };

        Player player = null;
        if (strings[0].equals("clear")) {
            if (strings.length > 2) {
                if (!sender.hasPermission("nautiluscosmetics.clear.other")) {
                    sender.sendMessage(Component.text("Not enough permissions").style(Style.style(NautilusCosmetics.ERROR_COLOR)));
                    return;
                }

                if ((player = Bukkit.getPlayerExact(strings[2])) == null) {
                    sender.sendMessage(Component.text("Player not found").style(Style.style(NautilusCosmetics.ERROR_COLOR)));
                    return;
                }
            }
        }

        if (player == null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("You must be or specify a player to run this command").style(Style.style(NautilusCosmetics.ERROR_COLOR)));
                return;
            }
            player = (Player) sender;
        }

        if (strings[1].equals("color")) {
            NameColor.setNameColor(player, FancyText.ColorType.SOLID, true, TextColor.color(255, 255, 255));
            return;
        } else if (strings[1].equals("nickname")) {
            Nickname.setNickname(player, Component.text(player.getName()), true);
            return;
        }

        sender.sendMessage(getUsageMessage(strings));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();

        if (strings.length == 1) {
            out.add("menu");
            out.add("set");
            out.add("clear");
        } else if (strings.length == 2) {
            if (strings[0].equals("set") || strings[0].equals("clear")) {
                out.addAll(SUBCOMMANDS.keySet());
            }
        } else if (strings.length == 3) {
            if (strings[0].equals("set")) {
                if (strings[1].equals("color")) {
                    for (FancyText.ColorType type : FancyText.ColorType.values()) {
                        if (commandSender.hasPermission("nautiluscosmetics.color."+type.name().toLowerCase())) out.add(type.name().toLowerCase());
                    }
                }
            }
        }

        int idx = strings.length >= 2 && SUBCOMMANDS.containsKey(strings[1]) ? SUBCOMMANDS.get(strings[1])+3 : -1;

        Optional<FancyText.ColorType> type;
        if (idx != -1 && strings.length > 2 && strings[1].equals("color") && (type = Arrays.stream(FancyText.ColorType.values()).filter(t->t.name().equalsIgnoreCase(strings[2])).findFirst()).isPresent()) {
            idx += type.get().numColors-1;
            if (strings.length-3 <= type.get().numColors) {
                out.addAll(Arrays.stream(ChatFormatting.values()).filter(ChatFormatting::isColor).map(ChatFormatting::getName).toList());
                out.add("#FFFFFF");
            }
        }

        if (idx != -1 && strings[0].equals("set") && strings.length == idx && commandSender.hasPermission("nautiluscosmetics.set.other")
        ) {
            out.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
        }

        return out.stream().filter(str->str.startsWith(strings[strings.length-1])).toList();
    }
}
