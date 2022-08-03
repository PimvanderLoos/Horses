package nl.pim16aap2.horses.util;

import nl.pim16aap2.horses.Horses;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

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
}
