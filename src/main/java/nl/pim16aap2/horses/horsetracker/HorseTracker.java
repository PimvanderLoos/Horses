package nl.pim16aap2.horses.horsetracker;

import nl.pim16aap2.horses.Communicator;
import nl.pim16aap2.horses.Config;
import nl.pim16aap2.horses.HorseEditor;
import nl.pim16aap2.horses.Horses;
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
    private final Communicator communicator;
    private Map<UUID, HorseStatus> trackedHorses = new HashMap<>();
    private @Nullable BukkitTask task;

    @Inject
    public HorseTracker(Horses plugin, Config config, HorseEditor horseEditor, Communicator communicator)
    {
        this.plugin = plugin;
        this.config = config;
        this.horseEditor = horseEditor;
        this.communicator = communicator;
    }

    public void trackHorse(AbstractHorse horse)
    {
        if (task == null)
            return;
        trackedHorses.computeIfAbsent(horse.getUniqueId(), uuid ->
            new HorseStatus(horse, config.getEnergyDrainTime(), config.getEnergyRecoveryTime()));
    }

    private void processTrackedHorses()
    {
        final Iterator<HorseStatus> it = trackedHorses.values().iterator();
        while (it.hasNext())
        {
            final HorseStatus status = it.next();
            if (status.isEnergyFull())
            {
                horseEditor.setExhausted(status.getHorse(), false);
                if (status.isRiderless())
                {
                    it.remove();
                    continue;
                }
            }
            doStatusTick(status);
        }
    }

    private void doStatusTick(HorseStatus status)
    {
        if (horseEditor.getGait(status.getHorse()) >= 100 && !status.isRiderless())
            status.decreaseEnergy();
        else
            status.increaseEnergy();

        if (status.isExhausted())
            horseEditor.setExhausted(status.getHorse(), true);

        // No reason to spam too many packets.
        if (status.getHorse().getTicksLived() % 10 == 0)
        {
            final float percentage = status.getEnergyPercentage();
            for (final Player player : status.getRiders())
                communicator.sendEnergyPercentage(player, percentage);
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
                final HorseStatus oldStatus = entry.getValue();
                final HorseStatus newStatus =
                    new HorseStatus(oldStatus.getHorse(), config.getEnergyDrainTime(), config.getEnergyRecoveryTime());
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
                trackHorse(horse);
        }
    }

    private void stopTask()
    {
        if (task != null)
        {
            task.cancel();
            trackedHorses.values().forEach(horseStatus -> horseEditor.setExhausted(horseStatus.getHorse(), false));
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
