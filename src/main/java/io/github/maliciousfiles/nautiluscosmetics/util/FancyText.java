package io.github.maliciousfiles.nautiluscosmetics.util;

import com.google.common.collect.ImmutableMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.HSVLike;
import org.bukkit.Material;

import java.util.Map;

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

                /*
                 * text = "HelloWorld!!" (12)
                 * colors = [RED, BLUE, GREEN]
                 *
                 * section 0 = "HelloW" (6)
                 * section 1 = "orld!!" (6)
                 *
                 * i = 0, c = 'H', section = 0, colors = colors[0]-colors[1]
                 * i = 4, c = 'o', section = 1, colors = colors[1]-colors[2]
                 */

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

    public enum ColorType {
        SOLID(1),
        GRADIENT(2),
        ALTERNATING(2),
        RAINBOW(0);

        public final int numColors;

        ColorType(int numColors) {
            this.numColors = numColors;
        }
    }
}
