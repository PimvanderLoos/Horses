package nl.pim16aap2.horses.staminabar;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class StaminaNotifierManager
{
    private final Map<Player, IStaminaNotifier> notifierMap = new HashMap<>();

    @Inject
    public StaminaNotifierManager()
    {
    }

    public @Nullable IStaminaNotifier getNewNotifier(Player player, double staminaPercentage, boolean exhausted)
    {
        //noinspection SpellCheckingInspection
        if (!player.hasPermission("horses.user.staminabar"))
            return null;

        final IStaminaNotifier notifier = new StaminaBar(player, staminaPercentage, exhausted);
        final @Nullable IStaminaNotifier oldNotifier = notifierMap.put(player, notifier);
        if (oldNotifier != null)
            oldNotifier.kill();
        return notifier;
    }

    public void removeNotifier(Player player)
    {
        final @Nullable IStaminaNotifier notifier = notifierMap.get(player);
        if (notifier != null)
            notifier.kill();
    }
}
