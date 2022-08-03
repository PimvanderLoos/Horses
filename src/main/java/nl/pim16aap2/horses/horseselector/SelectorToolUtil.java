package nl.pim16aap2.horses.horseselector;

import nl.pim16aap2.horses.Config;
import nl.pim16aap2.horses.Horses;
import nl.pim16aap2.horses.util.Localizer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class SelectorToolUtil
{
    private final Horses plugin;
    private final Config config;
    private final Localizer localizer;
    private final NamespacedKey keySelectorTool;

    @Inject SelectorToolUtil(Horses plugin, Config config, Localizer localizer)
    {
        this.plugin = plugin;
        this.config = config;
        this.localizer = localizer;
        this.keySelectorTool = new NamespacedKey(plugin, "selectorTool");
    }

    public boolean isSelectorTool(@Nullable ItemStack itemStack)
    {
        if (itemStack == null)
            return false;
        final @Nullable ItemMeta meta = itemStack.getItemMeta();
        if (meta == null)
            return false;
        return meta.getPersistentDataContainer().has(keySelectorTool, PersistentDataType.BYTE);
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
        final ItemStack tool = newSelectorTool();
        if (tool.getType() == Material.AIR || tool.getAmount() == 0)
        {
            plugin.getLogger().severe("Failed to create tool! Received: '" + tool + "'");
            player.sendMessage(ChatColor.RED + localizer.get("notification.error.generic"));
            return false;
        }

        final PlayerInventory inventory = player.getInventory();
        final int selectedSlot = inventory.getHeldItemSlot();
        if (inventory.getItem(selectedSlot) == null)
            inventory.setItem(selectedSlot, tool);
        else if (inventory.addItem(tool).size() > 0)
        {
            player.sendMessage(ChatColor.RED + localizer.get("selector.error.inventory_full"));
            return false;
        }
        return true;
    }

    public void removeSelectorTool(Player player)
    {
        player.getInventory().forEach(
            item ->
            {
                if (isSelectorTool(item))
                    item.setAmount(0);
            });
    }

    public ItemStack newSelectorTool()
    {
        final ItemStack item = new ItemStack(config.getSelectorMaterial(), 1);

        item.addUnsafeEnchantment(Enchantment.LUCK, 99);

        final @Nullable ItemMeta meta = item.getItemMeta();
        if (meta == null)
        {
            plugin.getLogger().severe("Failed to get item meta from item: '" + item + "'");
            return new ItemStack(Material.AIR);
        }

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(localizer.get("selector.name"));
        //noinspection RegExpRedundantEscape
        meta.setLore(List.of(localizer.get("selector.lore").split("\\\n")));
        meta.getPersistentDataContainer().set(keySelectorTool, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }
}
