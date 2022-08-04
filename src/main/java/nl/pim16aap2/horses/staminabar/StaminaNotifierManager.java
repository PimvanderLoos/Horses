package nl.pim16aap2.horses.staminabar;

import nl.pim16aap2.horses.util.Localizer;
import nl.pim16aap2.horses.util.Permission;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class StaminaNotifierManager
{
    private final Map<Player, IStaminaNotifier> notifierMap = new HashMap<>();
    private final Localizer localizer;

    @Inject
    public StaminaNotifierManager(Localizer localizer)
    {
        this.localizer = localizer;
    }

    public @Nullable IStaminaNotifier getNewNotifier(Player player, double staminaPercentage, boolean exhausted)
    {
        if (!Permission.USER_SEE_STAMINA_BAR.isSetFor(player))
            return null;

        final IStaminaNotifier notifier = new StaminaBar(localizer, player, staminaPercentage, exhausted);
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

    public void removeAll()
    {
        notifierMap.values().forEach(IStaminaNotifier::kill);
    }
}
