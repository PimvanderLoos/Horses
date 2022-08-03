package nl.pim16aap2.horses.listeners;

import nl.pim16aap2.horses.Horses;
import nl.pim16aap2.horses.horseselector.HorseSelectorManager;
import nl.pim16aap2.horses.horseselector.SelectorToolUtil;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class SelectorToolListener implements Listener
{
    private final SelectorToolUtil selectorToolUtil;
    private final HorseSelectorManager horseSelectorManager;

    @Inject SelectorToolListener(SelectorToolUtil selectorToolUtil, HorseSelectorManager horseSelectorManager)
    {
        this.selectorToolUtil = selectorToolUtil;
        this.horseSelectorManager = horseSelectorManager;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSelect(EntityDamageByEntityEvent event)
    {
        if (!(event.getDamager() instanceof Player player) ||
            !(Horses.MONITORED_TYPES.contains(event.getEntity().getType())) ||
            !(event.getEntity() instanceof AbstractHorse horse))
            return;

        if (!selectorToolUtil.isSelectorTool(player.getInventory().getItemInMainHand()))
            return;

        event.setCancelled(true);
        horseSelectorManager.provideInput(player, horse);
    }
}
