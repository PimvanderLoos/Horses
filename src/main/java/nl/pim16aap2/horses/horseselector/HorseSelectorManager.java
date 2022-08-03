package nl.pim16aap2.horses.horseselector;

import nl.pim16aap2.horses.Horses;
import nl.pim16aap2.horses.util.IReloadable;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class HorseSelectorManager implements IReloadable
{
    private final Map<UUID, SelectionWaiter> waiter = new ConcurrentHashMap<>();
    private final Horses plugin;

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
}
