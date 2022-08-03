package nl.pim16aap2.horses.horseselector;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SelectorToolUtil
{
    @Inject SelectorToolUtil()
    {
    }

    public boolean isSelectorTool(@Nullable ItemStack itemStack)
    {
        return false;
    }

    /**
     * Attempts to give a selector tool to the player.
     *
     * @param player
     *     The player that is supposed to receive a new selector tool.
     * @return True if the selector tool was given to the player or false it could not be given for some reason (e.g.
     * inventory is full.)
     */
    public boolean giveNewSelectorTool(Player player)
    {
        return false;
    }

    public ItemStack newSelectorTool()
    {
        return new ItemStack(Material.AIR, 0);
    }
}
