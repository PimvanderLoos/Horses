package nl.pim16aap2.horses.horseselector;

import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

final class SelectionWaiter
{
    private final Player player;
    private final BiConsumer<Player, AbstractHorse> method;
    private final AtomicBoolean completed = new AtomicBoolean(false);

    public SelectionWaiter(Player player, BiConsumer<Player, AbstractHorse> method)
    {
        this.player = player;
        this.method = method;
    }

    public void provideInput(AbstractHorse horse)
    {
        if (!completed.compareAndSet(false, true))
            return;
        method.accept(player, horse);
    }

    public boolean isCompleted()
    {
        return completed.get();
    }
}
