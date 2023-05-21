package io.github.maliciousfiles.nautiluscosmetics.util;

import com.google.common.collect.ImmutableMap;
import io.github.maliciousfiles.nautiluscosmetics.NautilusCosmetics;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.HSVLike;
import net.minecraft.ChatFormatting;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.function.Predicate;

public class FancyText {
    public static final Map<Material, TextColor> DYE_COLORS = ImmutableMap.ofEntries(
            Map.entry(Material.WHITE_DYE, TextColor.color(252, 252, 252)),
            Map.entry(Material.LIGHT_GRAY_DYE, TextColor.color(214, 214, 214)),
            Map.entry(Material.GRAY_DYE, TextColor.color(130, 130, 130)),
            Map.entry(Material.BLACK_DYE, TextColor.color(39, 39, 50)),
            Map.entry(Material.BROWN_DYE, TextColor.color(151, 92, 50)),
            Map.entry(Material.RED_DYE, TextColor.color(207, 67, 62)),
            Map.entry(Material.ORANGE_DYE, TextColor.color(227, 156, 51)),
            Map.entry(Material.YELLOW_DYE, TextColor.color(228, 228, 41)),
            Map.entry(Material.LIME_DYE, TextColor.color(129, 209, 28)),
            Map.entry(Material.GREEN_DYE, TextColor.color(73, 106, 24)),
            Map.entry(Material.CYAN_DYE, TextColor.color(44, 123, 155)),
            Map.entry(Material.LIGHT_BLUE_DYE, TextColor.color(141, 183, 241)),
            Map.entry(Material.BLUE_DYE, TextColor.color(51, 93, 193)),
            Map.entry(Material.PURPLE_DYE, TextColor.color(162, 82, 204)),
            Map.entry(Material.MAGENTA_DYE, TextColor.color(201, 104, 195)),
            Map.entry(Material.PINK_DYE, TextColor.color(234, 165, 201))
    );

    public static Material getClosestDye(TextColor color) {
        Material closest = Material.WHITE_DYE;
        double closestDist = Double.MAX_VALUE;

        for (Map.Entry<Material, TextColor> entry : DYE_COLORS.entrySet()) {
            double dist = Math.sqrt(Math.pow(color.red() - entry.getValue().red(), 2) + Math.pow(color.green() - entry.getValue().green(), 2) + Math.pow(color.blue() - entry.getValue().blue(), 2));

            if (dist < closestDist) {
                closest = entry.getKey();
                closestDist = dist;
            }
        }

        return closest;
    }

    public static Component colorText(ColorType type, String text, TextColor... colors) {
        switch(type) {
            case GRADIENT -> {
                Component out = Component.empty();

                int sectionLen = text.length() / (colors.length-1);
                for (int i = 0; i < text.length(); i++) {
                    int section = i/sectionLen;
                    out = out.append(Component.text(text.charAt(i)).color(TextColor.lerp((i % sectionLen) / (float) sectionLen, colors[section], colors[section+1])));
                }

                return out;
            }
            case ALTERNATING -> {
                Component out = Component.empty();

                for (int i = 0; i < text.length(); i++) {
                    out = out.append(Component.text(text.charAt(i)).color(colors[i%colors.length]));
                }

                return out;
            }
            case RAINBOW -> {
                Component out = Component.empty();

                for (int i = 0; i < text.length(); i++) {
                    out = out.append(Component.text(text.charAt(i)).color(TextColor.color(HSVLike.hsvLike(0.9f/text.length() * i, 0.725f, 1))));
                }

                return out;
            }
            default -> {
                return Component.text(text).color(colors[0]);
            }
        }
    }

    public static Component parseChatFormatting(String message) {
        Component component = Component.empty();
        Component building = Component.empty();

        for (int i = 0; i < message.length(); i++) {
            boolean consumed = false;

            if ((i == 0 || message.charAt(i-1) != '\\') && message.charAt(i) == '`' && i < message.length()-1) {
                if (message.charAt(i+1) == 'x') {
                    try {
                        int hex = Integer.parseInt(message.substring(i+2, i+8), 16);
                        component = component.append(building);
                        building = Component.empty().style(building.style()).color(TextColor.color(hex));
                        i += 7;
                        consumed = true;
                    } catch (NumberFormatException ignored) {}
                } else {
                    ChatFormatting formatting = ChatFormatting.getByCode(message.charAt(i+1));

                    if (message.charAt(i+1) == '`') {
                        for (ChatFormatting f : ChatFormatting.values()) {
                            if (message.substring(i+2).toUpperCase().startsWith(f.name())) {
                                formatting = f;
                                i += f.name().length();
                                consumed = true;
                            }
                        }
                    }

                    if (formatting != null) {
                        component = component.append(building);
                        building = NautilusCosmetics.format(Component.empty().style(formatting != ChatFormatting.RESET ? building.style() : Style.empty()), formatting);

                        i++;
                        consumed = true;
                    }
                }
            }

            if (!consumed) {
                building = building.append(Component.text(message.charAt(i)));
            }
        }
        
        return component.append(building);
    }

    public enum ColorType {
        SOLID(1, Material.BLUE_TERRACOTTA, TextColor.color(168, 131, 255)),
        GRADIENT(2, Material.MAGENTA_GLAZED_TERRACOTTA, TextColor.color(255, 156, 253), TextColor.color(0x56ABFF)),
        ALTERNATING(2, Material.CYAN_GLAZED_TERRACOTTA, TextColor.color(27, 255, 197), TextColor.color(0x28B592)),
        RAINBOW(0, "end/dragon_egg", Material.ORANGE_GLAZED_TERRACOTTA);

        public final int numColors;
        public final Advancement advancementReq;

        private final ItemStack example;

        ColorType(int numColors, Material material, TextColor... colors) {
            this(numColors, null, material, colors);
        }

        ColorType(int numColors, String advancementReq, Material material, TextColor... colors) {
            this.numColors = numColors;
            this.advancementReq = Bukkit.getAdvancement(NamespacedKey.fromString(advancementReq));

            this.example = new ItemStack(material);
            ItemMeta meta = example.getItemMeta();

            meta.displayName(FancyText
                    .colorText(this, WordUtils.capitalizeFully(name().replace("_", " ")), colors)
                    .decoration(TextDecoration.ITALIC, false));

            example.setItemMeta(meta);
        }

        // returns `null` if `sender` has access, otherwise returns the error string
        public String hasAccess(CommandSender sender) {
            if (sender instanceof Player player && advancementReq != null) {
                if (!player.getAdvancementProgress(advancementReq).isDone()) {
                    return "Complete " + advancementReq.displayName() + " to unlock!";
                }

                return null;
            }

            if (!sender.hasPermission("nautiluscosmetics.color."+name().toLowerCase())) {
                return NautilusCosmetics.SPONSOR_PERM_MESSAGE;
            }

            return null;
        }

        public ItemStack exampleItem() {
            return example.clone();
        }
    }
}
