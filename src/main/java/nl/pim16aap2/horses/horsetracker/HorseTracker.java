package nl.pim16aap2.horses.horsetracker;

import nl.pim16aap2.horses.Communicator;
import nl.pim16aap2.horses.Config;
import nl.pim16aap2.horses.HorseEditor;
import nl.pim16aap2.horses.Horses;
import nl.pim16aap2.horses.staminabar.StaminaNotifierManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class HorseTracker
{
    private final Horses plugin;
    private final Config config;
    private final HorseEditor horseEditor;
    private final StaminaNotifierManager staminaNotifierManager;
    private Map<UUID, TrackedHorse> trackedHorses = new HashMap<>();
    private @Nullable BukkitTask task;

    @Inject
    public HorseTracker(
        Horses plugin, Config config, HorseEditor horseEditor, Communicator communicator, StaminaNotifierManager staminaNotifierManager)
    {
        this.plugin = plugin;
        this.config = config;
        this.horseEditor = horseEditor;
        this.staminaNotifierManager = staminaNotifierManager;
    }

    public void trackHorse(Player rider, AbstractHorse horse)
    {
        if (task == null)
            return;
        trackedHorses.computeIfAbsent(horse.getUniqueId(), uuid ->
            new TrackedHorse(
                horse, staminaNotifierManager.getNewNotifier(rider), config.getEnergyDrainTime(),
                config.getEnergyRecoveryTime()));
    }

    private void processTrackedHorses()
    {
        final Iterator<TrackedHorse> it = trackedHorses.values().iterator();
        while (it.hasNext())
        {
            final TrackedHorse trackedHorse = it.next();
            if (trackedHorse.isEnergyFull())
            {
                horseEditor.setExhausted(trackedHorse.getHorse(), false);
                if (trackedHorse.isRiderless())
                {
                    it.remove();
                    continue;
                }
            }
            doStatusTick(trackedHorse);
        }
    }

    private void doStatusTick(TrackedHorse trackedHorse)
    {
        if (horseEditor.getGait(trackedHorse.getHorse()) >= 100 && !trackedHorse.isRiderless())
            trackedHorse.decreaseEnergy();
        else
            trackedHorse.increaseEnergy();

        if (trackedHorse.outOfEnergy())
            horseEditor.setExhausted(trackedHorse.getHorse(), true);

        if (trackedHorse.getHorse().getTicksLived() % 4 == 0)
        {
            final float percentage = trackedHorse.getEnergyPercentage();
            for (final Player player : trackedHorse.getRiders())
                if (trackedHorse.getStaminaNotifier() != null)
                    trackedHorse.getStaminaNotifier().notifyStaminaChange(
                        player, percentage, horseEditor.isExhausted(trackedHorse.getHorse()));
        }
    }

    private void startTask()
    {
        if (task == null)
        {
            task = Bukkit.getScheduler().runTaskTimer(plugin, this::processTrackedHorses, 0L, 1L);
            findHorsesWithRiders();
        }
        else
            updateTrackedHorses();
    }

    private void updateTrackedHorses()
    {
        trackedHorses = trackedHorses.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            entry ->
            {
                final TrackedHorse oldStatus = entry.getValue();
                final TrackedHorse newStatus =
                    new TrackedHorse(oldStatus.getHorse(), oldStatus.getStaminaNotifier(),
                                     config.getEnergyDrainTime(), config.getEnergyRecoveryTime());
                newStatus.setEnergyPercentage(oldStatus.getEnergyPercentage());
                return newStatus;
            }));
    }

    private void findHorsesWithRiders()
    {
        for (final Player player : Bukkit.getOnlinePlayers())
        {
            final @Nullable Entity vehicle = player.getVehicle();
            if (vehicle != null &&
                Horses.MONITORED_TYPES.contains(vehicle.getType()) &&
                vehicle instanceof AbstractHorse horse)
                trackHorse(player, horse);
        }
    }

    private void stopTask()
    {
        if (task != null)
        {
            task.cancel();
            trackedHorses.values().forEach(trackedHorse -> horseEditor.setExhausted(trackedHorse.getHorse(), false));
            trackedHorses.clear();
            task = null;
        }
    }

    public void onEnable()
    {
        if (config.getExhaustionPenalty() > 0)
            startTask();
        else
            stopTask();
    }
}
