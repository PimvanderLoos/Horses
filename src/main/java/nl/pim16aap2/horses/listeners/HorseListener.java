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
import org.spigotmc.event.entity.EntityDismountEvent;

public class HorseListener implements Listener
{
    private final Horses plugin;
    private final Config config;
    private final HorseEditor horseEditor;

    public HorseListener(Horses plugin, Config config, HorseEditor horseEditor)
    {
        this.plugin = plugin;
        this.config = config;
        this.horseEditor = horseEditor;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInfoClick(EntityDamageByEntityEvent event)
    {
        if (event.getDamager() instanceof Player player && processInfoClick(player, event.getEntity()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onMountedInfoClick(PlayerInteractEvent event)
    {
        final @Nullable Entity vehicle = event.getPlayer().getVehicle();
        if (vehicle == null)
            return;

        if (processInfoClick(event.getPlayer(), vehicle))
            event.setCancelled(true);
    }

    /**
     * Processes an info click caused by a player with a target entity.
     *
     * @param player
     *     The player that caused the info click.
     * @param target
     *     The target entity of the info click. If this is not a type of horse, nothing happens.
     * @return If the info click was processed successfully.
     */
    private boolean processInfoClick(Player player, Entity target)
    {
        if (!(Horses.MONITORED_TYPES.contains(target.getType())) ||
            !(target instanceof AbstractHorse horse))
            return false;

        final ItemStack holding = player.getInventory().getItemInMainHand();
        if (holding.getType() != config.getInfoMaterial())
            return false;

        plugin.getCommunicator().printInfo(player, horse);
        return true;
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

    @EventHandler
    public void onDismount(EntityDismountEvent event)
    {
        if (config.getResetGait() < 0)
            return;
        if (!Horses.MONITORED_TYPES.contains(event.getDismounted().getType()) ||
            !(event.getDismounted() instanceof AbstractHorse horse))
            return;
        horseEditor.setGait(horse, config.getResetGait());
    }
}
