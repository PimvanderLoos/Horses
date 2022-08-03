package nl.pim16aap2.horses.baby;

import nl.pim16aap2.horses.Config;
import nl.pim16aap2.horses.HorseEditor;
import nl.pim16aap2.horses.util.Localizer;
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

        if (config.alternativeAgeMethod())
            child.setAgeLock(true);

        return true;
    }

    /**
     * Checks if this plugin should hijack interactions of items on a horse.
     *
     * @param horse
     *     The target horse.
     * @param material
     *     The material being used on the horse.
     * @return True if this plugin should hijack the interaction.
     */
    public boolean hijackInteraction(AbstractHorse horse, Material material)
    {
        if (horse.isAdult() || !horse.getAgeLock())
            return false;
        return DEFAULT_FEED_MATERIALS.contains(material) || config.getBabyFoodMap().containsKey(material);
    }

    public void feedBaby(Player player, AbstractHorse horse, ItemStack item)
    {
        if (!horse.getAgeLock())
            return;

        final @Nullable Float percentage = config.getBabyFoodMap().get(item.getType());
        if (percentage == null)
            return;

        if (!Util.checkPlayerAccess(player, horse, "horses.admin.feed_baby"))
        {
            player.sendMessage(ChatColor.RED + localizer.get("notification.error.not_allowed_to_feed_baby"));
            return;
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
    }

    private void particlesOnFeed(AbstractHorse horse, ItemStack itemStack)
    {
        // Default materials already play the particle, so we can ignore those.
        if (DEFAULT_FEED_MATERIALS.contains(itemStack.getType()))
            return;
        horse.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, horse.getLocation(), 1, 0.2, 0.2, 0.2, 0.1);
    }
}
