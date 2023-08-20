package nl.pim16aap2.horses.listeners;


import nl.pim16aap2.horses.Communicator;
import nl.pim16aap2.horses.Config;
import nl.pim16aap2.horses.HorseEditor;
import nl.pim16aap2.horses.HorseGender;
import nl.pim16aap2.horses.baby.BabyHandler;
import nl.pim16aap2.horses.horseselector.SelectorToolUtil;
import nl.pim16aap2.horses.horsetracker.HorseTracker;
import nl.pim16aap2.horses.staminabar.StaminaNotifierManager;
import nl.pim16aap2.horses.util.Localizer;
import nl.pim16aap2.horses.util.Permission;
import nl.pim16aap2.horses.util.Util;
import org.bukkit.ChatColor;
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
import org.bukkit.event.entity.EntitySpawnEvent;
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
    private final SelectorToolUtil selectorToolUtil;
    private final Localizer localizer;
    private final Communicator communicator;

    @Inject HorseListener(
        Config config, HorseEditor horseEditor, Communicator communicator, HorseTracker horseTracker,
        StaminaNotifierManager staminaNotifierManager, BabyHandler babyHandler, SelectorToolUtil selectorToolUtil,
        Localizer localizer)
    {
        this.config = config;
        this.horseEditor = horseEditor;
        this.communicator = communicator;
        this.horseTracker = horseTracker;
        this.staminaNotifierManager = staminaNotifierManager;
        this.babyHandler = babyHandler;
        this.selectorToolUtil = selectorToolUtil;
        this.localizer = localizer;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInfoClick(EntityDamageByEntityEvent event)
    {
        if (event.getDamager() instanceof Player player && processInfoClick(player, event.getEntity()))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onShearClick(EntityDamageByEntityEvent event)
    {
        if (!(event.getDamager() instanceof Player player) ||
            !(event.getEntity() instanceof AbstractHorse horse) ||
            !(config.getMonitoredTypes().contains(horse.getType())))
            return;

        if (player.getInventory().getItemInMainHand().getType() != Material.SHEARS)
            return;

        event.setCancelled(true);
        if (horseEditor.getGender(horse) != HorseGender.MALE)
            return;

        if (!Permission.USER_MAKE_GELDING.isSetFor(player))
        {
            player.sendMessage(ChatColor.RED + localizer.get("notification.error.no_permission_for_gelding"));
            return;
        }

        if (!Util.checkPlayerAccess(player, horse, Permission.ADMIN_MAKE_GELDING))
        {
            player.sendMessage(ChatColor.RED + localizer.get("notification.error.generic_no_access"));
            return;
        }

        event.setCancelled(false);
        event.setDamage(0);
        horseEditor.setGender(horse, HorseGender.GELDING);
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
        if (!(target instanceof AbstractHorse horse) ||
            !(config.getMonitoredTypes().contains(target.getType())))
            return false;

        final ItemStack holding = player.getInventory().getItemInMainHand();
        if (holding.getType() != config.getInfoMaterial() || selectorToolUtil.isSelectorTool(holding))
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

    @EventHandler
    public void onSpawn(EntitySpawnEvent event)
    {
        if (!(event.getEntity() instanceof AbstractHorse horse) ||
            !config.getMonitoredTypes().contains(horse.getType()))
            return;

        if (horse.isAdult())
            horseEditor.ensureHorseManaged(horse);
        else
            babyHandler.newBaby(horse, null, null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreed(EntityBreedEvent event)
    {
        if (!(event.getFather() instanceof AbstractHorse father) ||
            !config.getMonitoredTypes().contains(father.getType()) ||
            !(event.getMother() instanceof AbstractHorse mother) ||
            !config.getMonitoredTypes().contains(mother.getType()) ||
            !(event.getEntity() instanceof AbstractHorse child))
            return;
        event.setCancelled(!babyHandler.newBaby(child, father, mother));
    }

    @EventHandler(ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event)
    {
        if (!config.getMonitoredTypes().contains(event.getEntityType()))
            return;
        event.getDrops().removeIf(itemStack -> itemStack.getType().equals(Material.LEATHER));
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event)
    {
        if (!(event.getDismounted() instanceof AbstractHorse horse) ||
            !config.getMonitoredTypes().contains(horse.getType()))
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
        if (!(event.getMount() instanceof AbstractHorse horse) ||
            !config.getMonitoredTypes().contains(horse.getType()) ||
            !(event.getEntity() instanceof Player player))
            return;

        if (!Permission.USER_MOUNT.isSetFor(player))
        {
            player.sendMessage(ChatColor.RED + localizer.get("notification.error.generic_no_permission"));
            event.setCancelled(true);
            return;
        }

        if (!Util.checkPlayerAccess(player, horse, Permission.ADMIN_MOUNT))
        {
            player.sendMessage(ChatColor.RED + localizer.get("notification.error.generic_no_access"));
            event.setCancelled(true);
            return;
        }

        if (config.getExhaustionPenalty() <= 0)
            return;

        horseTracker.trackHorse(player, horse);
    }
}
