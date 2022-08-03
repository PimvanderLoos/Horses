package nl.pim16aap2.horses.horseselector;

import nl.pim16aap2.horses.Horses;
import nl.pim16aap2.horses.util.IReloadable;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class HorseSelectorManager implements IReloadable
{
    private final Horses plugin;
    private final Map<UUID, SelectionWaiter> waiters = new ConcurrentHashMap<>();

    @Inject HorseSelectorManager(Horses plugin)
    {
        this.plugin = plugin;
        plugin.registerReloadable(this);
    }

    public void provideInput(Player player, AbstractHorse horse)
    {
    }

    @Override
    public void reload()
    {
    }

    public boolean activeWaiterForPlayer(Player player)
    {
        final @Nullable SelectionWaiter waiter = waiters.get(player.getUniqueId());
        return waiter != null && !waiter.isCompleted();
    }
}
