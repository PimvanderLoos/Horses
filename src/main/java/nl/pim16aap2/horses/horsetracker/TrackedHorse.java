package nl.pim16aap2.horses.horsetracker;

import nl.pim16aap2.horses.Config;
import nl.pim16aap2.horses.HorseEditor;
import nl.pim16aap2.horses.staminabar.IStaminaNotifier;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

final class TrackedHorse
{
    private final Config config;
    private final HorseEditor horseEditor;
    private final AbstractHorse horse;
    private final int maxEnergy;
    private final int drainStep;
    private final int recoveryStep;

    private @Nullable IStaminaNotifier notifier;
    private @Nullable TrackingExhaustionParticles trackingExhaustionParticles;

    private int energy;
    private volatile boolean exhausted;

    TrackedHorse(
        HorseEditor horseEditor, AbstractHorse horse, @Nullable IStaminaNotifier staminaNotifier, Config config)
    {
        this.config = config;
        this.horseEditor = horseEditor;
        this.horse = horse;
        this.notifier = staminaNotifier;

        this.drainStep = 20 * config.getEnergyRecoveryTime();
        this.recoveryStep = 20 * config.getEnergyDrainTime();
        this.maxEnergy = drainStep * recoveryStep;

        this.energy = maxEnergy;
    }

    TrackedHorse(TrackedHorse other, Config config)
    {
        this(other.horseEditor, other.horse, other.getStaminaNotifier(), config);

        trackingExhaustionParticles = other.trackingExhaustionParticles;
        exhausted = other.exhausted;

        setEnergyPercentage(other.getEnergyPercentage());
    }

    public float getEnergyPercentage()
    {
        return energy / (float) maxEnergy;
    }

    public boolean isEnergyFull()
    {
        return energy == maxEnergy;
    }

    public boolean outOfEnergy()
    {
        return energy == 0;
    }

    public void decreaseEnergy()
    {
        this.energy = Math.max(0, this.energy - drainStep);
    }

    public void increaseEnergy()
    {
        this.energy = Math.min(maxEnergy, this.energy + recoveryStep);
    }

    public void setEnergyPercentage(float percentage)
    {
        this.energy = (int) Math.min(maxEnergy, Math.max(0, this.maxEnergy * percentage));
    }

    public List<Player> getRiders()
    {
        return horse.getPassengers().stream()
                    .filter(Player.class::isInstance)
                    .map(Player.class::cast).toList();
    }

    public boolean isRiderless()
    {
        return horse.getPassengers().isEmpty();
    }

    public @Nullable IStaminaNotifier getStaminaNotifier()
    {
        return notifier;
    }

    public void setStaminaNotifier(@Nullable IStaminaNotifier staminaNotifier)
    {
        this.notifier = staminaNotifier;
    }

    public void setExhausted(boolean exhausted)
    {
        if (exhausted != this.exhausted)
            onExhaustionChange(exhausted);
        horseEditor.setExhausted(horse, exhausted);
        this.exhausted = exhausted;
    }

    public boolean isExhausted()
    {
        return exhausted;
    }

    public int getGait()
    {
        return horseEditor.getGait(horse);
    }

    public int getTicksLived()
    {
        return horse.getTicksLived();
    }

    private void onExhaustionChange(boolean newValue)
    {
        if (newValue)
            startParticles();
        else
            stopParticles();
    }

    private void startParticles()
    {
        if (trackingExhaustionParticles == null)
            trackingExhaustionParticles = new TrackingExhaustionParticles(this, config);
        else
            trackingExhaustionParticles.restart(this);
    }

    private void stopParticles()
    {
        if (trackingExhaustionParticles != null)
            trackingExhaustionParticles.cancel();
    }

    public AbstractHorse getTrackedEntity()
    {
        return horse;
    }

    @Override
    public boolean equals(@Nullable Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof TrackedHorse other))
            return false;
        return energy == other.energy && horse.equals(other.horse);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(horse, energy);
    }

    @Override
    public String toString()
    {
        return "HorseStatus{" +
            "horse=" + horse +
            ", maxEnergy=" + maxEnergy +
            ", drainStep=" + drainStep +
            ", recoveryStep=" + recoveryStep +
            ", energy=" + energy +
            '}';
    }
}
