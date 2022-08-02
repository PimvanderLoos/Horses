package nl.pim16aap2.horses.staminabar;

import org.bukkit.entity.Player;

public interface IStaminaNotifier
{
    void notifyStaminaChange(Player player, double percentage, boolean exhausted);

    default void kill()
    {
    }
}
