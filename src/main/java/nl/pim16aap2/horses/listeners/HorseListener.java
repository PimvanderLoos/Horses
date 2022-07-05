package nl.pim16aap2.horses.listeners;


import nl.pim16aap2.horses.Config;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Set;

public class HorseListener implements Listener
{
    private final Set<EntityType> monitoredTypes = EnumSet.of(EntityType.HORSE, EntityType.MULE, EntityType.DONKEY);
    private final Config config;

    public HorseListener(Config config)
    {
        this.config = config;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInfoClick(EntityDamageByEntityEvent event)
    {
        if (!(monitoredTypes.contains(event.getEntityType())) || !(event.getDamager() instanceof Player player))
            return;

        final ItemStack holding = player.getInventory().getItemInMainHand();
        if (holding.getType() != config.getInfoMaterial())
            return;

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onWhipClick(PlayerInteractEvent event)
    {
        if (!event.getMaterial().equals(config.getWhipMaterial()))
            return;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreed(EntityBreedEvent event)
    {
        if (!monitoredTypes.contains(event.getMother().getType()))
            return;
    }
}
