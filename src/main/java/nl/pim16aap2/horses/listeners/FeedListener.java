package nl.pim16aap2.horses.listeners;

import nl.pim16aap2.horses.Config;
import nl.pim16aap2.horses.Horses;
import nl.pim16aap2.horses.baby.BabyHandler;
import nl.pim16aap2.horses.util.Localizer;
import nl.pim16aap2.horses.util.Permission;
import nl.pim16aap2.horses.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

@Singleton
class FeedListener implements Listener
{
    private static final Set<Material> VANILLA_FOODS = Set.of(
        Material.SUGAR, Material.WHEAT, Material.APPLE, Material.GOLDEN_CARROT, Material.GOLDEN_APPLE);

    private final Config config;
    private final BabyHandler babyHandler;
    private final Localizer localizer;

    @Inject FeedListener(Config config, BabyHandler babyHandler, Localizer localizer)
    {
        this.config = config;
        this.babyHandler = babyHandler;
        this.localizer = localizer;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFeed(PlayerInteractEntityEvent event)
    {
        if (!(event.getRightClicked() instanceof AbstractHorse horse) ||
            !Horses.MONITORED_TYPES.contains(horse.getType()))
            return;

        final ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
        event.setCancelled(horse.isAdult() ?
                           processAdultFeed(horse, event.getPlayer(), item) :
                           processFeedBaby(horse, event.getPlayer(), item));
        if (event.isCancelled())
            event.getPlayer().updateInventory();
    }

    /**
     * Attempts to feed an adult horse.
     *
     * @return True if the plugin manages the feeding process, and the vanilla event can be cancelled.
     */
    private boolean processAdultFeed(AbstractHorse horse, Player player, ItemStack item)
    {
        final FoodType foodType = isFoodItem(item);
        if (foodType == FoodType.NOT_FOOD)
            return false;

        if (!Permission.USER_FEED_ADULT.isSetFor(player))
        {
            player.sendMessage(ChatColor.RED + localizer.get("notification.error.generic_no_permission"));
            return true;
        }
        if (!Util.checkPlayerAccess(player, horse, Permission.ADMIN_FEED_ADULT))
        {
            player.sendMessage(ChatColor.RED + localizer.get("notification.error.generic_no_access"));
            return true;
        }

        if (foodType == FoodType.BREEDING_FOOD)
            return false;

        if (!config.allowFeeding())
            return true;

        if (foodType == FoodType.VANILLA_FOOD)
            return false;

        playFeedingAdultEffects(horse.getWorld(), horse);
        item.setAmount(item.getAmount() - 1);
        return true;
    }

    private void playFeedingAdultEffects(World world, AbstractHorse horse)
    {
        final Location loc = horse.getLocation().add(0D, 1.7D, 0D);
        // Spawn the particles slightly offset in front of the horse.
        loc.add(loc.getDirection().normalize().multiply(0.6));

        playEatingSound(horse, 1F, 1F);
        world.spawnParticle(Particle.VILLAGER_HAPPY, loc, 7, 0.35, 0.5, 0.35);
    }

    /**
     * Attempts to feed a baby horse.
     *
     * @return True if the plugin manages the feeding process, and the vanilla event can be cancelled.
     */
    private boolean processFeedBaby(AbstractHorse horse, Player player, ItemStack item)
    {
        if (!babyHandler.hijackInteraction(horse, item.getType()))
            return false;

        babyHandler.feedBaby(player, horse, item);
        playEatingSound(horse, 0.85F, 1.8F);
        return true;
    }

    private void playEatingSound(AbstractHorse horse, float volume, float pitch)
    {
        final Sound sound;
        if (horse instanceof Donkey)
            sound = Sound.ENTITY_DONKEY_EAT;
        else if (horse instanceof Mule)
            sound = Sound.ENTITY_MULE_EAT;
        else
            sound = Sound.ENTITY_HORSE_EAT;
        horse.getWorld().playSound(horse.getLocation(), sound, SoundCategory.NEUTRAL, volume, pitch);
    }

    private FoodType isFoodItem(ItemStack item)
    {
        if (config.getFoodItems().contains(item.getType()))
            return FoodType.CONFIGURED;
        if (item.getType() == Material.GOLDEN_CARROT ||
            item.getType() == Material.GOLDEN_APPLE ||
            item.getType() == Material.ENCHANTED_GOLDEN_APPLE)
            return FoodType.BREEDING_FOOD;
        if (VANILLA_FOODS.contains(item.getType()))
            return FoodType.VANILLA_FOOD;
        return FoodType.NOT_FOOD;
    }

    private enum FoodType
    {
        NOT_FOOD,
        CONFIGURED,
        BREEDING_FOOD,
        VANILLA_FOOD
    }
}
