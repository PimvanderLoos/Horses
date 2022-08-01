package nl.pim16aap2.horses.horsetracker;

import nl.pim16aap2.horses.Horses;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicInteger;

class TrackingExhaustionParticles
{
    private BukkitTask task;

    TrackingExhaustionParticles(TrackedHorse trackedHorse)
    {
        task = newTask(trackedHorse);
    }

    void cancel()
    {
        task.cancel();
    }

    void restart(TrackedHorse trackedHorse)
    {
        task.cancel();
        task = newTask(trackedHorse);
    }

    private BukkitTask newTask(TrackedHorse trackedHorse)
    {
        final Runnable runnable = new ParticlesRunnable(trackedHorse.getTrackedEntity());
        return Bukkit.getScheduler().runTaskTimerAsynchronously(Horses.instance(), runnable, 0L, 1L);
    }

    private static final class ParticlesRunnable implements Runnable
    {
        private static final int UPDATE_FREQUENCY = 2;

        private final Entity trackedEntity;
        private final AtomicInteger tickCount = new AtomicInteger(0);
        private final World world;

        public ParticlesRunnable(Entity trackedEntity)
        {
            this.trackedEntity = trackedEntity;
            this.world = trackedEntity.getWorld();
        }

        @Override
        public void run()
        {
            if (tickCount.incrementAndGet() % UPDATE_FREQUENCY != 0)
                return;

            final Location loc = trackedEntity.getLocation().add(0D, 1.3D, 0D);
            // Spawn the particles slightly offset in front of the horse.
            loc.add(loc.getDirection().normalize().multiply(0.85));

            world.spawnParticle(Particle.SMOKE_NORMAL, loc, 8, 0.25, 0.6, 0.25, 0.04);
            world.spawnParticle(Particle.SMOKE_LARGE, loc, 2, 0.1, 0.2, 0.1, 0.01);
        }
    }
}
