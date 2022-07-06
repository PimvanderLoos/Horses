package nl.pim16aap2.horses;

import org.bukkit.ChatColor;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class HorseEditor
{
    private final Config config;

    public HorseEditor(Config config)
    {
        this.config = config;
    }

    public void printInfo(Player player, AbstractHorse horse)
    {
        player.sendMessage(ChatColor.BLUE + "Issa " + horse.getType().name().toLowerCase(Locale.ENGLISH));
    }

    private void communicateSpeedChange(Player player, int newSpeed)
    {
        player.sendMessage(ChatColor.RED + "NEW SPEED: " + newSpeed);
    }

    public void increaseSpeed(Player player, AbstractHorse horse)
    {
        final int newSpeed = config.getGaits().getHigherGait(getCurrentSpeed(horse));
        setSpeed(horse, newSpeed);
        communicateSpeedChange(player, newSpeed);
    }

    public void decreaseSpeed(Player player, AbstractHorse horse)
    {
        final int newSpeed = config.getGaits().getLowerGait(getCurrentSpeed(horse));
        setSpeed(horse, newSpeed);
        communicateSpeedChange(player, newSpeed);
    }

    public int getCurrentSpeed(AbstractHorse horse)
    {
        return 100;
    }

    public void setSpeed(AbstractHorse horse, int speed)
    {

    }

    public boolean canBreed(AbstractHorse horseA, AbstractHorse horseB)
    {
        return getGender(horseA) != getGender(horseB);
    }

    public HorseGender getGender(AbstractHorse horse)
    {
        return HorseGender.MALE;
    }

    public void generateGender(AbstractHorse horse)
    {
    }
}
