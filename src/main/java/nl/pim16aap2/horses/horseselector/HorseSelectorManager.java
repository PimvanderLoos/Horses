package nl.pim16aap2.horses.horseselector;

import nl.pim16aap2.horses.Config;
import nl.pim16aap2.horses.Horses;
import nl.pim16aap2.horses.util.IReloadable;
import nl.pim16aap2.horses.util.Localizer;
import org.bukkit.Bukkit;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Singleton
public class HorseSelectorManager implements IReloadable
{
    private final Horses plugin;
    private final Localizer localizer;
    private final SelectorToolUtil selectorToolUtil;
    private final Config config;
    private final Map<UUID, SelectionWaiter> waiters = new ConcurrentHashMap<>();

    @Inject HorseSelectorManager(Horses plugin, Localizer localizer, SelectorToolUtil selectorToolUtil, Config config)
    {
        this.plugin = plugin;
        this.localizer = localizer;
        this.selectorToolUtil = selectorToolUtil;
        this.config = config;
        plugin.registerReloadable(this);
    }

    public void newWaiter(Player player, Consumer<AbstractHorse> method)
    {
        if (!selectorToolUtil.giveNewSelectorTool(player))
            return;

        final SelectionWaiter waiter = new SelectionWaiter(localizer, selectorToolUtil, player, method);

        final @Nullable SelectionWaiter oldWaiter = waiters.put(player.getUniqueId(), waiter);
        if (oldWaiter != null)
            oldWaiter.abortSilent();

        Bukkit.getScheduler().runTaskLater(plugin, () -> onTimeout(waiter), config.getSelectorTimeOut() * 20L);
    }

    private void onTimeout(SelectionWaiter waiter)
    {
        waiter.timeOut();
        // Only remove the waiter from the map if the mapped waiter is the same
        // instance as the waiter in the runnable. If it's not a match
        // it was overridden by a newer one, which we do not want to unmap.
        waiters.compute(waiter.getPlayer().getUniqueId(), (uuid, mappedWaiter) ->
            mappedWaiter == waiter ? null : mappedWaiter);
    }

    public void provideInput(Player player, AbstractHorse horse)
    {
        final @Nullable SelectionWaiter waiter = waiters.get(player.getUniqueId());
        if (waiter == null || waiter.isCompleted())
        {
            plugin.getLogger().severe("Received input for inactive waiter: " + waiter);
            return;
        }
        waiter.processInput(horse);
    }

    @Override
    public void reload()
    {
        waiters.values().forEach(SelectionWaiter::abort);
    }

    public boolean activeWaiterForPlayer(Player player)
    {
        final @Nullable SelectionWaiter waiter = waiters.get(player.getUniqueId());
        return waiter != null && !waiter.isCompleted();
    }
}
