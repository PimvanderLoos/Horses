package nl.pim16aap2.horses.horsetracker;

import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

final class HorseStatus
{
    private final AbstractHorse horse;
    private final int maxEnergy;
    private final int drainStep;
    private final int recoveryStep;
    private int energy;

    public HorseStatus(AbstractHorse horse, int drainTime, int recoveryTime)
    {
        this.horse = horse;

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

    public boolean isExhausted()
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

    public AbstractHorse getHorse()
    {
        return horse;
    }

    @Override
    public boolean equals(@Nullable Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof HorseStatus other))
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
