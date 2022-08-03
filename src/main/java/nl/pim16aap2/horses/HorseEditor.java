package nl.pim16aap2.horses;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Random;

@Singleton
public final class HorseEditor
{
    private static final HorseGender[] GENDERS = HorseGender.values();

    private final Random random = new Random();
    private final Config config;
    private final Horses plugin;
    private final NamespacedKey keyGender;
    private final NamespacedKey keyGait;
    private final NamespacedKey keyBaseSpeed;
    private final NamespacedKey keyExhausted;
    private final Provider<Communicator> communicatorProvider;

    private @Nullable Team team;

    @Inject
    public HorseEditor(Horses plugin, Config config, Provider<Communicator> communicatorProvider)
    {
        this.config = config;
        this.plugin = plugin;

        keyGender = new NamespacedKey(plugin, "gender");
        keyGait = new NamespacedKey(plugin, "gait");
        keyBaseSpeed = new NamespacedKey(plugin, "baseSpeed");
        keyExhausted = new NamespacedKey(plugin, "exhausted");
        this.communicatorProvider = communicatorProvider;
    }

    public void increaseGait(Player player, AbstractHorse horse)
    {
        final int newSpeed = config.getGaits().getHigherGait(getGait(horse));
        setGait(horse, newSpeed);
        communicatorProvider.get().communicateSpeedChange(player, newSpeed);
    }

    public void decreaseGait(Player player, AbstractHorse horse)
    {
        final int newSpeed = config.getGaits().getLowerGait(getGait(horse));
        setGait(horse, newSpeed);
        communicatorProvider.get().communicateSpeedChange(player, newSpeed);
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
        updateEffectiveSpeed(horse, gait, isExhausted(horse));
    }

    private void updateEffectiveSpeed(AbstractHorse horse, int gait, boolean isExhausted)
    {
        final double baseSpeed = getBaseSpeed(horse);
        final float exhaustionPenalty = isExhausted ? (1 - config.getExhaustionPenalty() / 100f) : 1f;
        final double effectiveSpeed = Math.max(0, baseSpeed * (gait / 100f) * exhaustionPenalty);

        final @Nullable AttributeInstance attribute = horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attribute == null)
        {
            plugin.getLogger().severe("Failed to set movement speed for horse!");
            return;
        }
        attribute.setBaseValue(effectiveSpeed);
    }

    public boolean isExhausted(AbstractHorse horse)
    {
        final @Nullable Byte exhausted = horse.getPersistentDataContainer().get(keyExhausted, PersistentDataType.BYTE);
        if (exhausted != null)
            return exhausted == 1;
        setExhausted(horse, false);
        return false;
    }

    public void setExhausted(AbstractHorse horse, boolean exhausted)
    {
        ensureHorseManaged(horse);

        final PersistentDataContainer container = horse.getPersistentDataContainer();
        container.set(keyExhausted, PersistentDataType.BYTE, (byte) (exhausted ? 1 : 0));
        updateEffectiveSpeed(horse, getGait(horse), exhausted);
    }

    public void setName(AbstractHorse horse, @Nullable String name)
    {
        horse.setCustomName(name);
    }

    public double getBaseSpeed(AbstractHorse horse)
    {
        final @Nullable Double speed = horse.getPersistentDataContainer().get(keyBaseSpeed, PersistentDataType.DOUBLE);
        if (speed == null)
            return assignBaseSpeed(horse);
        return speed;
    }

    /**
     * Sets the base speed of a horse.
     *
     * @param horse
     *     The horse whose base speed to change.
     * @param speed
     *     The new speed value, in blocks per second.
     */
    public void setBaseSpeed(AbstractHorse horse, double speed)
    {
        final double baseSpeed = speed / 43.17f;
        horse.getPersistentDataContainer().set(keyBaseSpeed, PersistentDataType.DOUBLE, baseSpeed);
        updateEffectiveSpeed(horse, getGait(horse), isExhausted(horse));
    }

    public boolean canBreed(AbstractHorse horseA, AbstractHorse horseB)
    {
        final HorseGender genderA = getGender(horseA);
        if (genderA == HorseGender.GELDING)
            return false;
        final HorseGender genderB = getGender(horseB);
        return genderB != HorseGender.GELDING && genderA != genderB;
    }

    public HorseGender getGender(AbstractHorse horse)
    {
        ensureHorseManaged(horse);

        final @Nullable Byte genderIdx = horse.getPersistentDataContainer().get(keyGender, PersistentDataType.BYTE);
        if (genderIdx == null)
            return assignGender(horse);
        return GENDERS[genderIdx];
    }

    private HorseGender assignGender(AbstractHorse horse)
    {
        final HorseGender gender = GENDERS[random.nextInt(2)];
        setGender(horse, gender);
        return gender;
    }

    public void setGender(AbstractHorse horse, HorseGender gender)
    {
        horse.getPersistentDataContainer().set(keyGender, PersistentDataType.BYTE, (byte) gender.ordinal());
    }

    public void setMaxHealth(AbstractHorse horse, double maxHealth)
    {
        final @Nullable AttributeInstance attribute = horse.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attribute == null)
        {
            plugin.getLogger().severe("Failed to set max health for horse!");
            return;
        }
        attribute.setBaseValue(maxHealth);
    }

    private double assignBaseSpeed(AbstractHorse horse)
    {
        final @Nullable AttributeInstance movementSpeedAttr = horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        final double baseSpeed = movementSpeedAttr == null ? 0.225d : movementSpeedAttr.getBaseValue();
        horse.getPersistentDataContainer().set(keyBaseSpeed, PersistentDataType.DOUBLE, baseSpeed);
        return baseSpeed;
    }

    private void assignToTeam(AbstractHorse horse)
    {
        final @Nullable Team team = getTeam();
        if (team == null)
            return;
        team.addEntry(horse.getUniqueId().toString());
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
        assignToTeam(horse);

        final PersistentDataContainer container = horse.getPersistentDataContainer();
        if (container.get(keyGender, PersistentDataType.BYTE) != null)
            return;

        assignToTeam(horse);
        horse.setCustomNameVisible(false);

        assignGender(horse);
        assignBaseSpeed(horse);

        final int gait = config.getDefaultGait();
        container.set(keyGait, PersistentDataType.INTEGER, gait);
    }

    private @Nullable Team getTeam()
    {
        if (team != null)
            return team;

        final @Nullable ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        if (scoreboardManager == null)
            return null;

        final Scoreboard scoreboard = scoreboardManager.getMainScoreboard();
        team = scoreboard.getTeam("horses_noNameTag");
        if (team == null)
            team = scoreboard.registerNewTeam("horses_noNameTag");
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        return team;
    }
}
