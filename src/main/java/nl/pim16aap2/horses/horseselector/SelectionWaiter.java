package nl.pim16aap2.horses.horseselector;

import nl.pim16aap2.horses.util.Localizer;
import org.bukkit.ChatColor;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

final class SelectionWaiter
{
    private final SelectorToolUtil selectorToolUtil;
    private final Player player;
    private final Consumer<AbstractHorse> method;
    private final AtomicBoolean completed = new AtomicBoolean(false);
    private final Localizer localizer;

    public SelectionWaiter(
        Localizer localizer, SelectorToolUtil selectorToolUtil, Player player, Consumer<AbstractHorse> method)
    {
        this.localizer = localizer;
        this.selectorToolUtil = selectorToolUtil;
        this.player = player;
        this.method = method;

        player.sendMessage(ChatColor.GREEN + localizer.get("selector.process.started"));
    }

    public void processInput(AbstractHorse horse)
    {
        if (!completed.compareAndSet(false, true))
            return;
        player.sendMessage(ChatColor.GREEN + localizer.get("selector.process.completed"));
        selectorToolUtil.removeSelectorTool(player);
        method.accept(horse);
    }

    public boolean isCompleted()
    {
        return completed.get();
    }

    /**
     * Marks the waiter as completed and removes the selector tool.
     *
     * @param announce
     *     Whether to announce to the player that the process was interrupted.
     * @return True if the process was aborted, false if it was already finished.
     */
    private boolean abort0(boolean announce)
    {
        if (completed.compareAndSet(false, true))
        {
            if (announce)
                player.sendMessage(ChatColor.RED + localizer.get("selector.process.interrupted"));
            selectorToolUtil.removeSelectorTool(player);
            return true;
        }
        return false;
    }

    public void timeOut()
    {
        if (abort0(false))
            player.sendMessage(ChatColor.RED + localizer.get("selector.process.timed_out"));
    }

    public void abort()
    {
        abort0(true);
    }

    public void abortSilent()
    {
        abort0(false);
    }

    public Player getPlayer()
    {
        return player;
    }

    @Override
    public String toString()
    {
        return "SelectionWaiter{" +
            "player=" + player +
            ", completed=" + isCompleted() +
            '}';
    }
}
