package nl.pim16aap2.horses;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Random;

public final class HorseEditor
{
    private static final HorseGender[] GENDERS = HorseGender.values();

    private final Random random = new Random();
    private final Config config;
    private final JavaPlugin javaPlugin;
    private final NamespacedKey keyGender;
    private final NamespacedKey keyGait;
    private final NamespacedKey keyBaseSpeed;

    public HorseEditor(JavaPlugin javaPlugin, Config config)
    {
        this.config = config;
        this.javaPlugin = javaPlugin;

        keyGender = new NamespacedKey(javaPlugin, "gender");
        keyGait = new NamespacedKey(javaPlugin, "gait");
        keyBaseSpeed = new NamespacedKey(javaPlugin, "baseSpeed");
    }

    public void printInfo(Player player, AbstractHorse horse)
    {
        ensureHorseManaged(horse);

        //noinspection deprecation
        final String msg = ChatColor.DARK_GRAY + ">>>>>>--------------------------<<<<<<<\n"
            + addInfo("Name", Objects.requireNonNullElse(horse.getCustomName(), horse.getType().getName()))
            + addInfo("Gender", config.getGenderName(getGender(horse)))
            + addInfo("Gait", getGait(horse))
            + addInfo("Speed", String.format("%.2f", getBaseSpeed(horse) * 43.17f))
            + addInfo("Jump", String.format("%.2f", horse.getJumpStrength()))
            + addInfo("Health", String.format("%.0f", horse.getHealth()))
            + addInfo("Owner", getOwnerName(horse))
            + ChatColor.DARK_GRAY + ">>>>>>--------------------------<<<<<<<\n";

        player.sendMessage(msg);
    }

    private String getOwnerName(AbstractHorse horse)
    {
        final @Nullable AnimalTamer owner = horse.getOwner();
        return owner == null ? "Unowned" : owner.getName();
    }

    private String addInfo(String name, Object value)
    {
        return ChatColor.GOLD + name + ": " + ChatColor.GRAY + value.toString() + "\n";
    }

    private void communicateSpeedChange(Player player, int newSpeed)
    {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                    new TextComponent(ChatColor.RED + "Speed: " + newSpeed));
    }

    public void increaseGait(Player player, AbstractHorse horse)
    {
        final int newSpeed = config.getGaits().getHigherGait(getGait(horse));
        setGait(horse, newSpeed);
        communicateSpeedChange(player, newSpeed);
    }

    public void decreaseGait(Player player, AbstractHorse horse)
    {
        final int newSpeed = config.getGaits().getLowerGait(getGait(horse));
        setGait(horse, newSpeed);
        communicateSpeedChange(player, newSpeed);
    }

    public int getGait(AbstractHorse horse)
    {
        ensureHorseManaged(horse);

        final PersistentDataContainer container = horse.getPersistentDataContainer();
        final @Nullable Integer gait = container.get(keyGait, PersistentDataType.INTEGER);
        if (gait != null)
            return gait;

        final int defaultGait = config.getDefaultGait();
        setGait(horse, defaultGait);
        return defaultGait;
    }

    public void setGait(AbstractHorse horse, int gait)
    {
        ensureHorseManaged(horse);

        final PersistentDataContainer container = horse.getPersistentDataContainer();
        container.set(keyGait, PersistentDataType.INTEGER, gait);
        updateEffectiveSpeed(horse, gait);
    }

    private void updateEffectiveSpeed(AbstractHorse horse, int gait)
    {
        final double baseSpeed = getBaseSpeed(horse);
        final double effectiveSpeed = baseSpeed * (gait / 100f);

        final @Nullable AttributeInstance attribute = horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attribute == null)
        {
            javaPlugin.getLogger().severe("Failed to set movement speed for horse!");
            return;
        }
        attribute.setBaseValue(effectiveSpeed);
    }

    public double getBaseSpeed(AbstractHorse horse)
    {
        final @Nullable Double speed = horse.getPersistentDataContainer().get(keyBaseSpeed, PersistentDataType.DOUBLE);
        if (speed == null)
            return assignBaseSpeed(horse);
        return speed;
    }

    public boolean canBreed(AbstractHorse horseA, AbstractHorse horseB)
    {
        return getGender(horseA) != getGender(horseB);
    }

    public HorseGender getGender(AbstractHorse horse)
    {
        ensureHorseManaged(horse);

        final @Nullable Byte genderIdx = horse.getPersistentDataContainer().get(keyGender, PersistentDataType.BYTE);
        if (genderIdx == null)
            return assignGender(horse.getPersistentDataContainer());
        return GENDERS[genderIdx];
    }

    private HorseGender assignGender(PersistentDataContainer container)
    {
        final HorseGender gender = random.nextBoolean() ? HorseGender.MALE : HorseGender.FEMALE;
        container.set(keyGender, PersistentDataType.BYTE, (byte) gender.ordinal());
        return gender;
    }

    private double assignBaseSpeed(AbstractHorse horse)
    {
        final @Nullable AttributeInstance movementSpeedAttr = horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        final double baseSpeed = movementSpeedAttr == null ? 0.225d : movementSpeedAttr.getBaseValue();
        horse.getPersistentDataContainer().set(keyBaseSpeed, PersistentDataType.DOUBLE, baseSpeed);
        return baseSpeed;
    }

    /**
     * Ensures this horse is 'managed', i.e. it has all required data set.
     * <p>
     * If the horse is not managed yet, its default values will be set.
     *
     * @param horse
     *     The horse to analyze.
     */
    public void ensureHorseManaged(AbstractHorse horse)
    {
        final PersistentDataContainer container = horse.getPersistentDataContainer();
        if (container.get(keyGender, PersistentDataType.BYTE) != null)
            return;

        horse.setCustomNameVisible(false);

        assignGender(container);
        assignBaseSpeed(horse);


        final int gait = config.getDefaultGait();
        container.set(keyGait, PersistentDataType.INTEGER, gait);
    }
}
