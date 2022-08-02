package nl.pim16aap2.horses.listeners;


import nl.pim16aap2.horses.Communicator;
import nl.pim16aap2.horses.Config;
import nl.pim16aap2.horses.HorseEditor;
import nl.pim16aap2.horses.Horses;
import nl.pim16aap2.horses.baby.BabyHandler;
import nl.pim16aap2.horses.horsetracker.HorseTracker;
import nl.pim16aap2.horses.staminabar.StaminaNotifierManager;
import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class HorseListener implements Listener
{
    private final Config config;
    private final HorseEditor horseEditor;
    private final HorseTracker horseTracker;
    private final StaminaNotifierManager staminaNotifierManager;
    private final BabyHandler babyHandler;
    private final Communicator communicator;

    @Inject HorseListener(
        Config config, HorseEditor horseEditor, Communicator communicator, HorseTracker horseTracker,
        StaminaNotifierManager staminaNotifierManager, BabyHandler babyHandler)
    {
        this.config = config;
        this.horseEditor = horseEditor;
        this.communicator = communicator;
        this.horseTracker = horseTracker;
        this.staminaNotifierManager = staminaNotifierManager;
        this.babyHandler = babyHandler;
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

        communicator.printInfo(player, horse);
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

        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
            horseEditor.increaseGait(event.getPlayer(), horse);
        else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
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
        event.setCancelled(!babyHandler.newBaby(horseA, horseB, child));
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
        if (!Horses.MONITORED_TYPES.contains(event.getDismounted().getType()) ||
            !(event.getDismounted() instanceof AbstractHorse horse))
            return;

        if (config.getResetGait() >= 0)
            horseEditor.setGait(horse, config.getResetGait());

        if (event.getEntity() instanceof Player player)
            staminaNotifierManager.removeNotifier(player);
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent event)
    {
        staminaNotifierManager.removeNotifier(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMount(EntityMountEvent event)
    {
        if (config.getExhaustionPenalty() <= 0)
            return;

        if (!Horses.MONITORED_TYPES.contains(event.getMount().getType()) ||
            !(event.getMount() instanceof AbstractHorse horse) ||
            !(event.getEntity() instanceof Player player))
            return;

        horseTracker.trackHorse(player, horse);
    }
}
