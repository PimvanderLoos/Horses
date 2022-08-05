package nl.pim16aap2.horses.listeners;

import nl.pim16aap2.horses.util.Util;
import org.bukkit.Location;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class TeleportListener implements Listener
{
    @Inject TeleportListener()
    {
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event)
    {
        final @Nullable Location target = event.getTo();
        if (target == null)
            return;

        if (event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN &&
            event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL &&
            event.getCause() != PlayerTeleportEvent.TeleportCause.END_PORTAL &&
            event.getCause() != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL &&
            event.getCause() != PlayerTeleportEvent.TeleportCause.COMMAND)
            return;

        final List<AbstractHorse> horses = Util.getLeadHorses(event.getPlayer());
        horses.forEach(horse -> horse.teleport(target));

        final @Nullable AbstractHorse riddenHorse = Util.getHorseRiddenByPlayer(event.getPlayer());
        if (riddenHorse != null)
        {
            riddenHorse.eject();
            riddenHorse.teleport(target);
        }
    }
}
