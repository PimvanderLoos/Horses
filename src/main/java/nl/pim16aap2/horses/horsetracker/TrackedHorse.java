package nl.pim16aap2.horses.horsetracker;

import nl.pim16aap2.horses.HorseEditor;
import nl.pim16aap2.horses.staminabar.IStaminaNotifier;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

final class TrackedHorse
{
    private final HorseEditor horseEditor;
    private final AbstractHorse horse;
    private final int maxEnergy;
    private @Nullable IStaminaNotifier notifier;
    private final int drainStep;
    private final int recoveryStep;
    private int energy;
    private boolean exhausted;

    public TrackedHorse(
        HorseEditor horseEditor, AbstractHorse horse, @Nullable IStaminaNotifier staminaNotifier, int drainTime,
        int recoveryTime)
    {
        this.horseEditor = horseEditor;
        this.horse = horse;
        this.notifier = staminaNotifier;

        this.drainStep = 20 * recoveryTime;
        this.recoveryStep = 20 * drainTime;
        this.maxEnergy = drainStep * recoveryStep;

        this.energy = maxEnergy;
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

    /**
     * Creates a new tracked horse that has its energy drain time and recovery time remapped to new values.
     * <p>
     * All other parameters such as the underlying horse or the current energy percentage are copied over.
     *
     * @param energyDrainTime
     *     The new amount of time it takes for the horse's energy to go from 100% to 0% measured in seconds.
     * @param energyRecoveryTime
     *     The new amount of time it takes for the horse's energy to go from 0% to 100% measured in seconds.
     * @return The new tracked horse, with its drain and recovery times remapped to new values.
     */
    public TrackedHorse remap(int energyDrainTime, int energyRecoveryTime)
    {
        final TrackedHorse ret = new TrackedHorse(horseEditor, horse, notifier, energyDrainTime, energyRecoveryTime);
        ret.setEnergyPercentage(getEnergyPercentage());
        return ret;
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
