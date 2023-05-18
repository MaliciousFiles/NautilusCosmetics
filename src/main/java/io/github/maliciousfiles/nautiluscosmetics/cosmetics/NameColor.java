package io.github.maliciousfiles.nautiluscosmetics.cosmetics;

import io.github.maliciousfiles.nautiluscosmetics.NautilusCosmetics;
import io.github.maliciousfiles.nautiluscosmetics.util.FancyText;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;

public class NameColor {

    public static final NameColor DEFAULT_COLOR = new NameColor(FancyText.ColorType.SOLID, TextColor.color(255, 255, 255));
    public static final NamespacedKey NAME_COLOR_KEY = new NamespacedKey(NautilusCosmetics.INSTANCE, "name_color");
    public static final PersistentDataType<PersistentDataContainer, NameColor> DATA_TYPE = new NameColorPersistentDataType();

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

    @Override
    public boolean equals(Object other) {
        return other instanceof NameColor color && color.type == type && Arrays.equals(color.colors, colors);
    }

    public static void init() {
        Bukkit.getPluginManager().registerEvents(new NameColor.NameColorListener(), NautilusCosmetics.INSTANCE);

        if (!NautilusCosmetics.SQL.isClosed()) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(NautilusCosmetics.INSTANCE, () -> {
                try {
                    Connection connection = NautilusCosmetics.SQL.getConnection();
                    Statement statement = connection.createStatement();

                    ResultSet results = statement.executeQuery("SELECT * FROM name_colors");
                    playerColors.clear();
                    while (results.next()) {
                        FancyText.ColorType type = FancyText.ColorType.values()[results.getInt("color_type")];
                        TextColor[] colors = new TextColor[type.numColors];

                        for (int i = 0; i < type.numColors; i++) {
                            colors[i] = TextColor.color(results.getInt("color" + (i + 1)));
                        }

                        playerColors.put(UUID.fromString(results.getString("uuid")), new NameColor(type, colors));
                    }

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        NameColor color = playerColors.getOrDefault(p.getUniqueId(), DEFAULT_COLOR);

                        if (!color.equals(p.getPersistentDataContainer().get(NAME_COLOR_KEY, DATA_TYPE))) {
                            setNameColor(p, color, false);
                        }
                    }

                    connection.close();
                } catch (SQLException e) {
                    Bukkit.getLogger().log(Level.SEVERE, "Failed to load nicknames!", e);
                }
            }, 0, NautilusCosmetics.SQL_UPDATE_TIME * 20);
        }
    }

    private static void setNameColor(UUID uuid, NameColor color) {
        if (color == null) playerColors.remove(uuid);
        else playerColors.put(uuid, color);

        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        if (p.isOnline()) {
            PersistentDataContainer data = p.getPlayer().getPersistentDataContainer();
            if (color == null) data.remove(NAME_COLOR_KEY);
            else data.set(NAME_COLOR_KEY, DATA_TYPE, color);
        }

        if (!NautilusCosmetics.SQL.isClosed()) {
            try {
                Connection connection = NautilusCosmetics.SQL.getConnection();
                Statement statement = connection.createStatement();

                if (color == null) {
                    statement.executeUpdate("DELETE FROM name_colors WHERE uuid='" + uuid + "'");
                } else {
                    StringBuilder command = new StringBuilder("INSERT INTO name_colors (uuid, color_type");
                    for (int i = 0; i < color.type.numColors; i++) command.append(", color").append(i + 1);
                    command.append(") VALUES ('").append(uuid).append("', ").append(color.type.ordinal());
                    for (int i = 0; i < color.type.numColors; i++) command.append(", ").append(color.colors[i].value());
                    command.append(") ON DUPLICATE KEY UPDATE color_type=").append(color.type.ordinal());
                    for (int i = 0; i < color.type.numColors; i++)
                        command.append(", color").append(i + 1).append("=").append(color.colors[i].value());

                    statement.executeUpdate(command.toString());
                }

                connection.close();
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to set nickname!", e);
            }
        }
    }

    public static void setNameColor(Player player, FancyText.ColorType type, boolean sendMessage, TextColor... colors) {
        NameColor color = new NameColor(type, colors);

        setNameColor(player.getUniqueId(), color.equals(DEFAULT_COLOR) ? null : color);

        setNameColor(player, color, sendMessage);
    }

    public static void setNameColor(Player player, NameColor color, boolean sendMessage) {
        if (!color.equals(getNameColor(player))) {
            player.displayName(FancyText.colorText(color.type, NautilusCosmetics.getTextContent(player.displayName()), color.colors));
            NautilusCosmetics.updateNameTag(player, player.displayName(), Bukkit.getOnlinePlayers());
        }

        if (sendMessage) player.sendMessage(FancyText.colorText(color.type, "Name color changed", color.colors));
    }

    public static class NameColorListener implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent e) {
            if (getNameColor(e.getPlayer()) != null) setNameColor(e.getPlayer(), getNameColor(e.getPlayer()), false);

            for (Map.Entry<UUID, NameColor> entry : playerColors.entrySet()) {
                Player p = Bukkit.getPlayer(entry.getKey());
                NautilusCosmetics.updateNameTag(p, p.displayName(), List.of(e.getPlayer()));
            }
        }
    }

    public static class NameColorPersistentDataType implements PersistentDataType<PersistentDataContainer, NameColor> {

        private static final NamespacedKey TYPE_KEY = new NamespacedKey(NautilusCosmetics.INSTANCE, "COLOR_TYPE");
        private static final NamespacedKey COLORS_KEY = new NamespacedKey(NautilusCosmetics.INSTANCE, "COLORS");

        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public @NotNull Class<NameColor> getComplexType() {
            return NameColor.class;
        }

        @Override
        public @NotNull PersistentDataContainer toPrimitive(@NotNull NameColor nameColor, @NotNull PersistentDataAdapterContext ctx) {
            PersistentDataContainer container = ctx.newPersistentDataContainer();

            container.set(TYPE_KEY, PersistentDataType.INTEGER, nameColor.type.ordinal());
            container.set(COLORS_KEY, PersistentDataType.INTEGER_ARRAY, Arrays.stream(nameColor.colors).mapToInt(TextColor::value).toArray());

            return container;
        }

        @Override
        public @NotNull NameColor fromPrimitive(@NotNull PersistentDataContainer container, @NotNull PersistentDataAdapterContext ctx) {
            return new NameColor(
                    FancyText.ColorType.values()[container.get(TYPE_KEY, PersistentDataType.INTEGER)],
                    Arrays.stream(container.get(COLORS_KEY, PersistentDataType.INTEGER_ARRAY))
                            .mapToObj(TextColor::color).toArray(TextColor[]::new));
        }
    }
}
