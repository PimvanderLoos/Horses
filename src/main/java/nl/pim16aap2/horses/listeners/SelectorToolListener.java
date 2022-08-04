package nl.pim16aap2.horses.listeners;

import nl.pim16aap2.horses.Horses;
import nl.pim16aap2.horses.horseselector.HorseSelectorManager;
import nl.pim16aap2.horses.horseselector.SelectorToolUtil;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class SelectorToolListener implements Listener
{
    private final SelectorToolUtil selectorToolUtil;
    private final HorseSelectorManager horseSelectorManager;

    @Inject SelectorToolListener(SelectorToolUtil selectorToolUtil, HorseSelectorManager horseSelectorManager)
    {
        this.selectorToolUtil = selectorToolUtil;
        this.horseSelectorManager = horseSelectorManager;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSelect(EntityDamageByEntityEvent event)
    {
        if (!(event.getDamager() instanceof Player player) ||
            !(Horses.MONITORED_TYPES.contains(event.getEntity().getType())) ||
            !(event.getEntity() instanceof AbstractHorse horse))
            return;

        if (!selectorToolUtil.isSelectorTool(player.getInventory().getItemInMainHand()))
            return;

        event.setCancelled(true);
        horseSelectorManager.provideInput(player, horse);
    }

    @EventHandler
    public void onItemDropEvent(PlayerDropItemEvent event)
    {
        if (selectorToolUtil.isSelectorTool(event.getItemDrop().getItemStack()))
        {
            if (horseSelectorManager.activeWaiterForPlayer(event.getPlayer()))
                event.getItemDrop().remove();
            else
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent event)
    {
        if (!selectorToolUtil.isSelectorTool(event.getCurrentItem()))
            return;

        final @Nullable Inventory clickedInventory = event.getClickedInventory();
        if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) ||
            (clickedInventory != null && !clickedInventory.getType().equals(InventoryType.PLAYER)))
        {
            if (event.getWhoClicked() instanceof Player)
            {
                if (horseSelectorManager.activeWaiterForPlayer((Player) event.getWhoClicked()))
                    event.getInventory().removeItem(event.getCurrentItem());
                else
                    event.setCancelled(true);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void inventoryDragEvent(InventoryDragEvent event)
    {
        event.getNewItems().forEach(
            (K, V) ->
            {
                if (selectorToolUtil.isSelectorTool(V))
                    event.setCancelled(true);
            });
    }

    @EventHandler
    public void onItemMoved(InventoryMoveItemEvent event)
    {
        if (!selectorToolUtil.isSelectorTool(event.getItem()))
            return;

        if (event.getSource() instanceof PlayerInventory playerInventory &&
            playerInventory.getHolder() instanceof Player player)
        {
            if (horseSelectorManager.activeWaiterForPlayer(player))
            {
                event.setCancelled(true);
                return;
            }
        }
        event.getSource().removeItem(event.getItem());
    }
}
