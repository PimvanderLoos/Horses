package nl.pim16aap2.horses.listeners;

import nl.pim16aap2.horses.Config;
import nl.pim16aap2.horses.util.Localizer;
import nl.pim16aap2.horses.util.Permission;
import nl.pim16aap2.horses.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerLeashEntityEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LeadRestrictionListener implements Listener
{
    private final Localizer localizer;
    private final Config config;

    @Inject LeadRestrictionListener(Localizer localizer, Config config)
    {
        this.localizer = localizer;
        this.config = config;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onLeadAttach(PlayerLeashEntityEvent event)
    {
        if (!(event.getEntity() instanceof AbstractHorse horse) ||
            !config.getMonitoredTypes().contains(horse.getType()))
            return;

        final Player player = event.getPlayer();
        if (!Permission.USER_LEAD.isSetFor(player))
        {
            player.sendMessage(ChatColor.RED + localizer.get("notification.error.generic_no_permission"));
            event.setCancelled(true);
            player.updateInventory();
            return;
        }

        if (!Util.checkPlayerAccess(player, horse, Permission.ADMIN_LEAD))
        {
            player.sendMessage(ChatColor.RED + localizer.get("notification.error.generic_no_access"));
            event.setCancelled(true);
            player.updateInventory();
            return;
        }
    }
}
