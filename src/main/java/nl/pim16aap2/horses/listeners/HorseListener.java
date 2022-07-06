package nl.pim16aap2.horses.listeners;


import nl.pim16aap2.horses.Config;
import nl.pim16aap2.horses.HorseEditor;
import nl.pim16aap2.horses.Horses;
import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class HorseListener implements Listener
{
    private final Config config;
    private final HorseEditor horseEditor;

    public HorseListener(Config config, HorseEditor horseEditor)
    {
        this.config = config;
        this.horseEditor = horseEditor;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInfoClick(EntityDamageByEntityEvent event)
    {
        if (!(Horses.MONITORED_TYPES.contains(event.getEntityType())) ||
            !(event.getDamager() instanceof Player player) ||
            !(event.getEntity() instanceof AbstractHorse horse))
            return;

        final ItemStack holding = player.getInventory().getItemInMainHand();
        if (holding.getType() != config.getInfoMaterial())
            return;

        event.setCancelled(true);
        horseEditor.printInfo(player, horse);
    }

    @EventHandler
    public void onWhipClick(PlayerInteractEvent event)
    {
        if (!event.getMaterial().equals(config.getWhipMaterial()))
            return;

        final @Nullable Entity vehicle = event.getPlayer().getVehicle();
        if (!(vehicle instanceof AbstractHorse horse))
            return;

        if (event.getAction() == Action.LEFT_CLICK_AIR)
            horseEditor.increaseGait(event.getPlayer(), horse);
        else if (event.getAction() == Action.RIGHT_CLICK_AIR)
            horseEditor.decreaseGait(event.getPlayer(), horse);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreed(EntityBreedEvent event)
    {
        if (!Horses.MONITORED_TYPES.contains(event.getMother().getType()) ||
            !(event.getFather() instanceof AbstractHorse horseA) ||
            !(event.getMother() instanceof AbstractHorse horseB) ||
            !(event.getEntity() instanceof AbstractHorse child))
            return;
        final boolean canBreed = horseEditor.canBreed(horseA, horseB);
        event.setCancelled(!canBreed);
        if (canBreed)
            horseEditor.ensureHorseManaged(child);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event)
    {
        if (!Horses.MONITORED_TYPES.contains(event.getEntityType()))
            return;
        event.getDrops().removeIf(itemStack -> itemStack.getType().equals(Material.LEATHER));
    }
}
