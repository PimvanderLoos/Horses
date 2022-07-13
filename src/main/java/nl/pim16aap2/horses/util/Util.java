package nl.pim16aap2.horses.util;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

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

    public static void spawnSmoke(Entity entity)
    {
        final Location loc = entity.getLocation().add(0, 1.3, 0);
        final World world = entity.getWorld();

        Bukkit.getScheduler().runTaskAsynchronously(Horses.instance(), () ->
        {
            for (int idx = 0; idx < 1_000; ++idx)
                world.playEffect(loc, Effect.SMOKE, 0, 8);
        });
    }
}
