package nl.pim16aap2.horses.baby;

import nl.pim16aap2.horses.Config;
import nl.pim16aap2.horses.HorseEditor;
import nl.pim16aap2.horses.util.Localizer;
import nl.pim16aap2.horses.util.Permission;
import nl.pim16aap2.horses.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

@Singleton
public class BabyHandler
{
    public static final Set<Material> DEFAULT_FEED_MATERIALS =
        Set.of(Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE, Material.GOLDEN_CARROT,
               Material.SUGAR, Material.WHEAT, Material.APPLE);

    private final HorseEditor horseEditor;
    private final Config config;
    private final Localizer localizer;

    @Inject BabyHandler(HorseEditor horseEditor, Config config, Localizer localizer)
    {
        this.horseEditor = horseEditor;
        this.config = config;
        this.localizer = localizer;
    }

    /**
     * Handles a new baby being born.
     *
     * @param child
     *     The child that would be born if the action is allowed.
     * @param horseA
     *     The first parent.
     * @param horseB
     *     The second parent.
     * @return True if the new baby is allowed to be born, otherwise false.
     */
    public boolean newBaby(AbstractHorse child, @Nullable AbstractHorse horseA, @Nullable AbstractHorse horseB)
    {
        if (horseA != null && horseB != null)
        {
            final boolean canBreed = horseEditor.canBreed(horseA, horseB);
            if (!canBreed)
                return false;
        }

        horseEditor.ensureHorseManaged(child);

        if (horseA != null && horseB != null)
        {
            horseEditor.setParents(child, horseA, horseB);
            setCorrectBaseSpeed(child, horseA, horseB);
        }

        if (config.alternativeAgeMethod())
            child.setAgeLock(true);

        return true;
    }

    /**
     * Ensures the speed value of the baby is correct given the two parents.
     * <p>
     * The gait affects the speed value of the horses, so the speed inherited by the baby is affected by the gaits of
     * the parents, which makes no sense.
     * <p>
     * As such, this method will recalculate the speed of the child to ensure it is based on the parents' base speed.
     *
     * @param child
     *     The child whose speed to change.
     * @param horseA
     *     The first parent.
     * @param horseB
     *     The second parent.
     */
    private void setCorrectBaseSpeed(AbstractHorse child, AbstractHorse horseA, AbstractHorse horseB)
    {
        final double speedA = horseEditor.getEffectiveSpeed(horseA);
        final double speedB = horseEditor.getEffectiveSpeed(horseB);
        final double oldSpeed = horseEditor.getBaseSpeed(child);
        final double bonus = oldSpeed * 3 - speedA - speedB;

        final double baseSpeedA = horseEditor.getBaseSpeed(horseA);
        final double baseSpeedB = horseEditor.getBaseSpeed(horseB);
        final double newSpeed = (bonus + baseSpeedA + baseSpeedB) / 3;

        horseEditor.setRawBaseSpeed(child, newSpeed);
    }

    /**
     * Attempts to feed and grow a baby.
     *
     * @param player
     *     The player responsible for feeding the baby.
     * @param horse
     *     The horse being fed.
     * @param item
     *     The item being fed to the horse.
     * @return True if the baby was fed.
     */
    public boolean tryFeedBaby(Player player, AbstractHorse horse, ItemStack item)
    {
        if (horse.isAdult() || !horse.getAgeLock())
            return false;

        final @Nullable Float percentage = config.getBabyFoodMap().get(item.getType());
        if (percentage == null)
            return false;

        if (!Util.checkPlayerAccess(player, horse, Permission.ADMIN_FEED_BABY))
        {
            player.sendMessage(ChatColor.RED + localizer.get("notification.error.not_allowed_to_feed_baby"));
            return false;
        }

        // Baby animals start with an age of -24_000 ticks.
        // When they reach an age of 0, they become adults.
        final int ageIncrease = (int) (percentage * 240F);
        horse.setAge(horse.getAge() + ageIncrease);

        player.sendMessage(
            ChatColor.GREEN + localizer.get("notification.feed_success", Util.formatBabyGrowthPercentage(horse)));

        if (player.getGameMode() != GameMode.CREATIVE)
            item.setAmount(item.getAmount() - 1);

        particlesOnFeed(horse, item);
        return true;
    }

    private void particlesOnFeed(AbstractHorse horse, ItemStack itemStack)
    {
        // Default materials already play the particle, so we can ignore those.
        if (DEFAULT_FEED_MATERIALS.contains(itemStack.getType()))
            return;
        horse.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, horse.getLocation(), 1, 0.2, 0.2, 0.2, 0.1);
    }
}
