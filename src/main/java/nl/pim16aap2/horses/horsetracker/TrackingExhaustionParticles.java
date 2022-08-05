package nl.pim16aap2.horses.horsetracker;

import nl.pim16aap2.horses.Config;
import nl.pim16aap2.horses.Horses;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

class TrackingExhaustionParticles
{
    private final Config config;

    private BukkitTask task;

    TrackingExhaustionParticles(TrackedHorse trackedHorse, Config config)
    {
        this.config = config;
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
        final BukkitRunnable runnable = new ParticlesRunnable(config, trackedHorse.getTrackedEntity());
        return runnable.runTaskTimerAsynchronously(Horses.instance(), 0L, 1L);
    }

    private static final class ParticlesRunnable extends BukkitRunnable
    {
        private final Config config;
        private final Entity trackedEntity;
        private final World world;

        public ParticlesRunnable(Config config, Entity trackedEntity)
        {
            this.config = config;
            this.trackedEntity = trackedEntity;
            this.world = trackedEntity.getWorld();
        }

        @Override
        public synchronized void cancel()
            throws IllegalStateException
        {
            super.cancel();
        }

        @Override
        public void run()
        {
            if (!trackedEntity.isValid())
                cancel();

            final Location loc = trackedEntity.getLocation().add(0D, 1.3D, 0D);
            // Spawn the particles slightly offset in front of the horse.
            loc.add(loc.getDirection().normalize().multiply(0.85));

            world.spawnParticle(
                Particle.SMOKE_NORMAL, loc, config.getExhaustionSmokeParticles(), 0.25, 0.6, 0.25, 0.04);
            world.spawnParticle(
                Particle.SMOKE_LARGE, loc, config.getExhaustionBigSmokeParticles(), 0.1, 0.2, 0.1, 0.01);
        }
    }
}
