package nl.pim16aap2.horses.horseselector;

import nl.pim16aap2.horses.util.Localizer;
import org.bukkit.ChatColor;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

final class SelectionWaiter
{
    private final Player player;
    private final BiConsumer<Player, AbstractHorse> method;
    private final AtomicBoolean completed = new AtomicBoolean(false);

    public SelectionWaiter(Localizer localizer, Player player, BiConsumer<Player, AbstractHorse> method)
    {
        this.player = player;
        this.method = method;

        player.sendMessage(ChatColor.GREEN + localizer.get("selector.process_started"));
    }

    public void processInput(AbstractHorse horse)
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
