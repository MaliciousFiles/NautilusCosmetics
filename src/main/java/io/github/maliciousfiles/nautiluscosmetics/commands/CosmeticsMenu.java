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
import net.minecraft.world.item.Items;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class CosmeticsMenu {

    private static GuiPage getSelectPage(Consumer<InventoryClickEvent> action) {
        BasicGuiPage page = new BasicGuiPage().setSize(5)
                .setName("Select Color")
                .addComponent(new BackGuiComponent()
                        .setItem(
                                new ItemStack(Material.ARROW),
                                Component.text("Back")
                                        .decoration(TextDecoration.ITALIC, false)
                        ), 4, 4)
                .addComponent(new OpenPageGuiComponent()
                        .setChildIdx(0)
                        .setItem(
                                new ItemStack(Material.PAPER),
                                Component.text("Custom Color")
                                        .decoration(TextDecoration.ITALIC, false)
                        ), 3, 7)
                .addChild(new TextInputGuiPage()
                        .setItem(new ItemStack(Material.NAME_TAG), Component.text("#FFFFFF"))
                        .setWindowName("Color or #Hexcode")
                        .setAction(action)
                        .setGenerateResult((anvil) -> {
                            net.minecraft.network.chat.TextColor color;
                            if ((color = net.minecraft.network.chat.TextColor.parseColor(anvil.itemName.toLowerCase().replace(' ', '_'))) != null) {
                                return new net.minecraft.world.item.ItemStack(Items.NAME_TAG)
                                        .setHoverName(net.minecraft.network.chat.Component.literal(WordUtils.capitalizeFully(color.toString().replace('_', ' '))).withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(color).withItalic(false)));
                            } else {
                                return net.minecraft.world.item.ItemStack.EMPTY;
                            }
                        }));

        int i = 0;
        for (Map.Entry<Material, TextColor> entry : FancyText.DYE_COLORS.entrySet()) {
            page.addComponent(new ButtonGuiComponent()
                    .setAction(action)
                    .setItem(
                            new ItemStack(entry.getKey()),
                            Component.text(WordUtils.capitalizeFully(entry.getKey().name().split("_DYE")[0].replace("_", " ")))
                                    .decoration(TextDecoration.ITALIC, false)
                                    .color(TextColor.color(entry.getValue()))
                    ), (i/7)+1, (i%7)+1);
            i++;
        }

        return page;
    }

    private static GuiPage selectColorsMenu(FancyText.ColorType type, NameColor current, int numColors, Consumer<TextColor[]> callback) {
        TextColor colors[] = new TextColor[numColors];
        Arrays.fill(colors, TextColor.color(255, 255, 255));

        BasicGuiPage page = new BasicGuiPage()
                .setSize(5)
                .setName("Select Colors")
                .addComponent(new BackGuiComponent()
                        .setItem(
                                new ItemStack(Material.ARROW),
                                Component.text("Back")
                                        .decoration(TextDecoration.ITALIC, false)
                        ), 4, 4);

        BiConsumer<Integer, TextColor> updateColor = (i, color) -> {
            colors[i] = color;
            page.addComponent(new OpenPageGuiComponent()
                            .setChildIdx(i)
                            .setItem(
                                    new ItemStack(FancyText.getClosestDye(color)),
                                    Component.text("Color " + (i + 1))
                                            .color(color)
                                            .decoration(TextDecoration.ITALIC, false),
                                    new Component[] {Component.text(color.asHexString())
                                            .color(TextColor.color(133, 194, 201))
                                            .decoration(TextDecoration.ITALIC, false)}
                            ), (i/3)+1, (i%3)*2+2)
                    .addComponent(new ButtonGuiComponent()
                            .setAction(ev -> callback.accept(colors))
                            .setCloseOnClick(true)
                            .setItem(
                                    new ItemStack(Material.NAME_TAG),
                                    FancyText.colorText(type, "Continue", colors)
                                            .decoration(TextDecoration.ITALIC, false)
                            ), 4, 5);
        };

        for (int i = 0; i < numColors; i++) {
            updateColor.accept(i, current != null && i < current.colors.length ? current.colors[i] : TextColor.color(255, 255, 255));

            int k = i;
            page.addChild(getSelectPage(e -> {
                updateColor.accept(k, TextColor.color(CraftItemStack.asNMSCopy(e.getCurrentItem()).getHoverName().getStyle().getColor().getValue()));
                page.open();
            }));
        }

        if (numColors == 2) {
            page.addComponent(new ButtonGuiComponent()
                    .setAction(e -> {
                        TextColor temp = colors[0];
                        updateColor.accept(0, colors[1]);
                        updateColor.accept(1, temp);
                        page.open();
                    })
                    .setItem(new ItemStack(Material.LEVER),
                            Component.text("Swap")
                                    .decoration(TextDecoration.ITALIC, false)), 4, 3);
        }

        return page;
    }

    private static GuiPage selectColorTypeMenu(Player sender) {
        BasicGuiPage page = new BasicGuiPage()
                .setSize(5)
                .setName("Select Color Type")
                .addComponent(new BackGuiComponent()
                        .setItem(
                                new ItemStack(Material.ARROW),
                                Component.text("Back")
                                        .decoration(TextDecoration.ITALIC, false)
                        ), 4, 4)
                .addComponent(new ButtonGuiComponent()
                        .setCloseOnClick(true)
                        .setAction(e->NameColor.setNameColor(sender, FancyText.ColorType.SOLID, true, TextColor.color(255, 255, 255)))
                        .setItem(
                                new ItemStack(Material.REDSTONE_TORCH),
                                Component.text("Reset")
                                        .color(NautilusCosmetics.ERROR_COLOR)
                                        .decoration(TextDecoration.ITALIC, false)
                        ), 3, 4);

        FancyText.ColorType[] colorType = new FancyText.ColorType[1];

        Map<FancyText.ColorType, Map.Entry<Material, Map.Entry<TextColor, TextColor>>> items = Map.of(
                FancyText.ColorType.SOLID, Map.entry(Material.BLUE_TERRACOTTA, Map.entry(TextColor.color(168, 131, 255), TextColor.color(0))),
                FancyText.ColorType.GRADIENT, Map.entry(Material.MAGENTA_GLAZED_TERRACOTTA, Map.entry(TextColor.color(255, 156, 253), TextColor.color(0x56ABFF))),
                FancyText.ColorType.ALTERNATING, Map.entry(Material.CYAN_GLAZED_TERRACOTTA, Map.entry(TextColor.color(27, 255, 197), TextColor.color(0x28B592))),
                FancyText.ColorType.RAINBOW, Map.entry(Material.ORANGE_GLAZED_TERRACOTTA, Map.entry(TextColor.color(0), TextColor.color(0)))
        );

        int i = 0;
        for (FancyText.ColorType type : FancyText.ColorType.values()) {
            Map.Entry<Material, Map.Entry<TextColor, TextColor>> entry = items.get(type);
            boolean hasPerm = sender.hasPermission("nautiluscosmetics.color." + type.name().toLowerCase());

            int k = i;
            page.addComponent(new ButtonGuiComponent()
                    .setAction(e -> {
                        if (hasPerm) {
                            if (type.numColors == 0) {
                                NameColor.setNameColor(sender, type, true);
                                return;
                            }

                            colorType[0] = type;
                            if (e.getClickedInventory().getHolder() instanceof BasicGuiPage p) {
                                p.getChild(k).open();
                            }
                        }
                    })
                    .setCloseOnClick(type.numColors == 0)
                    .setItem(
                            new ItemStack(hasPerm ? entry.getKey() : Material.BARRIER),
                            FancyText.colorText(type, WordUtils.capitalizeFully(type.name().replace("_", " ")), entry.getValue().getKey(), entry.getValue().getValue())
                                    .decoration(TextDecoration.ITALIC, false),
                            hasPerm ? new Component[]{} :
                                    new Component[]{
                                            Component.text("Become a sponsor to unlock!")
                                                    .color(NautilusCosmetics.ERROR_COLOR)
                                                    .decoration(TextDecoration.ITALIC, false)
                                    }
                    ), (i/3)+1, (i%3)*2+2);
            page.addChild(selectColorsMenu(type, NameColor.getNameColor(sender), type.numColors, colors -> NameColor.setNameColor(sender, type, true, colors)));

            i++;
        }

        return page;
    }

    public static void openMenu(CommandSender sender, Command command, String s, String[] strings) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can use this command").style(Style.style(NautilusCosmetics.ERROR_COLOR)));
            return;
        }

        Bukkit.getPluginManager().registerEvents(
                new Gui()
                        .setRoot(new BasicGuiPage()
                                .setSize(5)
                                .setName("Cosmetics")
                                .addComponent(new OpenPageGuiComponent()
                                        .setChildIdx(0)
                                        .setItem(
                                                new ItemStack(Material.NAME_TAG),
                                                Component.text("Name Color")
                                                        .color(TextColor.color(238, 69, 255))
                                                        .decoration(TextDecoration.ITALIC, false)
                                        ), 1, 1)
                                .addComponent(new OpenPageGuiComponent()
                                        .setChildIdx(1)
                                        .setItem(
                                                new ItemStack(Material.OAK_SIGN),
                                                Component.text("Nickname")
                                                        .color(TextColor.color(33, 245, 169))
                                                        .decoration(TextDecoration.ITALIC, false)
                                        ), 1, 3)
                                .addComponent(new BackGuiComponent()
                                        .setItem(
                                                new ItemStack(Material.RED_TERRACOTTA),
                                                Component.text("Exit")
                                                        .decoration(TextDecoration.ITALIC, false)
                                                        .color(NautilusCosmetics.ERROR_COLOR)
                                        ), 4, 4)
                                .addChild(selectColorTypeMenu((Player) sender))
                                .addChild(new TextInputGuiPage()
                                        .setItem(
                                                new ItemStack(Material.OAK_SIGN),
                                                sender.name().decoration(TextDecoration.ITALIC, false),
                                                Stream.of(
                                                        Component.text("Requirements").decorate(TextDecoration.UNDERLINED).decoration(TextDecoration.ITALIC, false).color(TextColor.color(101, 190, 201)),
                                                        Component.text(" - 3 characters or more").decoration(TextDecoration.ITALIC, false).color(TextColor.color(133, 194, 201)),
                                                        Component.text(" - 16 characters or less").decoration(TextDecoration.ITALIC, false).color(TextColor.color(133, 194, 201)),
                                                        Component.text(" - Cannot be the same as an existing player name or nickname").decoration(TextDecoration.ITALIC, false).color(TextColor.color(133, 194, 201)),
                                                        !sender.hasPermission("nautiluscosmetics.perks.nickname") ? Component.text(" - No special characters").decoration(TextDecoration.ITALIC, false).color(TextColor.color(133, 194, 201)) : null
                                                ).filter(Objects::nonNull).toArray(Component[]::new))
                                        .setWindowName("Nickname")
                                        .setAction(e-> Nickname.setNickname((Player) e.getWhoClicked(), e.getCurrentItem().getItemMeta().displayName(), true))
                                        .setCloseOnClick(true)
                                        .setGenerateResult((anvil) -> {
                                            if (Nickname.validateNickname((Player) sender, Component.text(anvil.itemName)) == null) {
                                                return new net.minecraft.world.item.ItemStack(Items.OAK_SIGN)
                                                        .setHoverName(net.minecraft.network.chat.Component.literal(anvil.itemName).setStyle(net.minecraft.network.chat.Style.EMPTY.withItalic(false)));
                                            } else {
                                                return net.minecraft.world.item.ItemStack.EMPTY;
                                            }
                                        })
                                )
                        ).display((Player) sender), NautilusCosmetics.INSTANCE);
    }
}
