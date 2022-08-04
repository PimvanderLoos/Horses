package nl.pim16aap2.horses.util;

import nl.pim16aap2.horses.Horses;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

public final class Util
{
    private Util()
    {
        // Utility class
    }

    public static String capitalizeFirstLetter(String str)
    {
        if (str.length() == 0)
            return str;
        if (str.length() == 1)
            return str.toUpperCase(Locale.ROOT);

        final var firstLetter = Character.toUpperCase(str.charAt(0));
        final String remaining = str.substring(1).toLowerCase(Locale.ROOT);
        return firstLetter + remaining;
    }

    public static @Nullable AbstractHorse getHorseRiddenByPlayer(Player player)
    {
        if (player.getVehicle() != null &&
            Horses.MONITORED_TYPES.contains(player.getVehicle().getType()) &&
            player.getVehicle() instanceof AbstractHorse horse)
            return horse;
        return null;
    }

    /**
     * Checks if a player has access to a horse.
     * <p>
     * A player is considered to have access to a horse if
     * <p>
     * 1) The player is an OP.
     * <p>
     * 2) The horse has no owner.
     * <p>
     * 3) The player is the owner of the horse.
     * <p>
     * 4) The provided permission node is not null and the player has that permission node.
     *
     * @param player
     *     The player to check.
     * @param horse
     *     The target horse.
     * @param permission
     *     The permission node required if the player is not an owner of the horse. May be null.
     * @return True if the player has access to the target horse.
     */
    public static boolean checkPlayerAccess(Player player, AbstractHorse horse, @Nullable String permission)
    {
        if (player.isOp())
            return true;
        if (horse.getOwner() == null || player.equals(horse.getOwner()))
            return true;
        return permission != null && player.hasPermission(permission);
    }

    public static String formatBabyGrowthPercentage(AbstractHorse horse)
    {
        final float percentage = Math.min(100, Math.max(0, (24_000 + horse.getAge()) / 240F));
        return String.format(Locale.ROOT, "%2.2f%%", percentage);
    }

    public static @Nullable UUID parseUUID(@Nullable String input)
    {
        if (input == null)
            return null;
        try
        {
            return UUID.fromString(input);
        }
        catch (IllegalArgumentException e)
        {
            Horses.instance().getLogger().severe("Failed to parse UUID from input: '" + input + "'!");
            return null;
        }
    }
}
