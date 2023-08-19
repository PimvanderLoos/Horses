package nl.pim16aap2.horses;

import nl.pim16aap2.horses.util.IReloadable;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Singleton
public class Config implements IReloadable
{
    private static final String DEFAULT_GAITS = "0,25,35,50,75,100";

    private final JavaPlugin javaPlugin;

    private Material infoMaterial = Material.FEATHER;
    private Material selectorMaterial = Material.FEATHER;
    private Material whipMaterial = Material.BLAZE_ROD;
    private Gaits gaits;
    private int defaultGait = 100;
    private int resetGait = -1;
    private boolean enableStaminaBar;
    private int exhaustionPenalty = 35;
    private int energyDrainTime = 5;
    private int energyRecoveryTime = 7;
    private int exhaustionSmokeParticles = 4;
    private int exhaustionBigSmokeParticles = 1;
    private int selectorTimeOut = 60;
    private boolean disableMountedSpeedPotionBuff = false;
    private boolean alternativeBabyGrowth = true;
    private boolean teleportHorses = true;
    private boolean restrictLeads = true;
    private boolean allowFeeding = true;
    private Set<Material> foodItems = Collections.emptySet();
    private Map<Material, Float> babyFoodMap = Collections.emptyMap();

    private final Path path;

    @Inject
    public Config(Horses horses)
    {
        this.javaPlugin = horses;
        gaits = parseGaits(DEFAULT_GAITS);
        this.path = horses.getDataFolder().toPath().resolve("config.yml");
        horses.registerReloadable(this);
    }

    @Override
    public void reload()
    {
        javaPlugin.getLogger().info("(Re)Loading config!");
        ensureFileExists();
        javaPlugin.reloadConfig();
        readValues(javaPlugin.getConfig());
    }

    private void readValues(FileConfiguration config)
    {
        this.infoMaterial = readMaterial(config, "infoMaterial", Material.FEATHER);
        this.selectorMaterial = readMaterial(config, "selectorMaterial", Material.FEATHER);
        this.whipMaterial = readMaterial(config, "whipMaterial", Material.BLAZE_ROD);

        this.gaits = parseGaits(config.getString("gaits", DEFAULT_GAITS));
        this.defaultGait = parseInt(config, "defaultGait", 100);
        this.resetGait = parseInt(config, "resetGait", 100);

        this.enableStaminaBar = config.getBoolean("enableStaminaBar", true);
        this.exhaustionPenalty = parseInt(config, "exhaustionPenalty", 25);
        this.energyDrainTime = parseInt(config, "energyDrainTime", 5);
        this.energyRecoveryTime = parseInt(config, "energyRecoveryTime", 7);

        this.selectorTimeOut = parseInt(config, "selectorTimeOut", 60);

        this.exhaustionSmokeParticles = parseInt(config, "exhaustionSmokeParticles", 4);
        this.exhaustionBigSmokeParticles = parseInt(config, "exhaustionBigSmokeParticles", 1);

        this.disableMountedSpeedPotionBuff = config.getBoolean("disableMountedSpeedPotionBuff", false);

        this.teleportHorses = config.getBoolean("teleportHorses", true);

        this.restrictLeads = config.getBoolean("restrictLeads", true);

        this.alternativeBabyGrowth = config.getBoolean("alternativeBabyGrowth", true);
        this.babyFoodMap = parseBabyFoodMap(config);

        this.allowFeeding = config.getBoolean("allowFeeding", true);
        this.foodItems = parseFoodItems(config);
    }

    private int parseInt(FileConfiguration configuration, String optionName, int fallback)
    {
        final @Nullable String value = configuration.getString(optionName);
        if (value == null)
        {
            javaPlugin.getLogger().severe("No value provided for option '" +
                                              optionName + "'! Using fallback: " + fallback);
            return fallback;
        }
        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            javaPlugin.getLogger().severe("Invalid integer '" + value + "' provided for option '" +
                                              optionName + "'! Using fallback: " + fallback);
        }
        return fallback;
    }

    private Gaits parseGaits(String line)
    {
        final String[] parts = line.split(",");
        final List<Integer> values = new ArrayList<>();
        for (final String part : parts)
        {
            try
            {
                values.add(Integer.parseInt(part));
            }
            catch (NumberFormatException e)
            {
                javaPlugin.getLogger().severe("Failed to parse number '" + part + "'");
            }
        }
        return new Gaits(values);
    }

    private Material readMaterial(FileConfiguration config, String optionName, Material fallback)
    {
        final @Nullable String materialName = config.getString(optionName, fallback.name());
        final @Nullable Material parsed = Material.getMaterial(materialName);
        if (parsed == null)
        {
            javaPlugin.getLogger().severe(
                "Failed to parse material with name '" + materialName + "' for option: '" + optionName +
                    "'! Defaulting to " + fallback.name());
            return fallback;
        }

        javaPlugin.getLogger().info("Selected material '" + parsed.name() + "' for option: '" + optionName + "'");
        return parsed;
    }

    private Map<Material, Float> parseBabyFoodMap(FileConfiguration config)
    {
        final Map<Material, Float> ret = new HashMap<>();
        final List<String> values = config.getStringList("babyFoodGrowthItems");

        for (final String line : values)
        {
            final String[] parts = line.split(":", 2);
            if (parts.length != 2)
            {
                javaPlugin.getLogger().severe("Invalid baby food configuration option '" + line + "'!");
                continue;
            }

            final @Nullable Material mat = Material.getMaterial(parts[0]);
            if (mat == null)
            {
                javaPlugin.getLogger().severe("Invalid material name '" + parts[0] + "'!");
                continue;
            }

            float percentage;
            try
            {
                percentage = Float.parseFloat(parts[1]);
            }
            catch (NumberFormatException e)
            {
                percentage = -1;
            }
            if (percentage < 0)
            {
                javaPlugin.getLogger().severe("Invalid amount: '" + parts[1] + "'!");
                continue;
            }
            ret.put(mat, percentage);
        }
        return Collections.unmodifiableMap(ret);
    }

    private Set<Material> parseFoodItems(FileConfiguration config)
    {
        final Set<Material> ret = new HashSet<>();
        final String values = config.getString("foodItems", "APPLE;WHEAT");
        final String[] names = values.split(";");
        for (final String name : names)
        {
            final @Nullable Material mat = Material.getMaterial(name);
            if (mat == null)
            {
                javaPlugin.getLogger().severe("Invalid material name '" + name + "'!");
                continue;
            }
            ret.add(mat);
        }
        return ret;
    }

    private void ensureFileExists()
    {
        if (Files.exists(this.path))
            return;
        javaPlugin.saveDefaultConfig();
    }

    public Material getInfoMaterial()
    {
        return infoMaterial;
    }

    public Material getSelectorMaterial()
    {
        return selectorMaterial;
    }

    public Material getWhipMaterial()
    {
        return whipMaterial;
    }

    public Gaits getGaits()
    {
        return gaits;
    }

    public int getDefaultGait()
    {
        return defaultGait;
    }

    public int getResetGait()
    {
        return resetGait;
    }

    public boolean enableStaminaBar()
    {
        return enableStaminaBar;
    }

    public int getExhaustionPenalty()
    {
        return exhaustionPenalty;
    }

    public int getEnergyDrainTime()
    {
        return energyDrainTime;
    }

    public int getEnergyRecoveryTime()
    {
        return energyRecoveryTime;
    }

    public int getSelectorTimeOut()
    {
        return selectorTimeOut;
    }

    public int getExhaustionSmokeParticles()
    {
        return exhaustionSmokeParticles;
    }

    public int getExhaustionBigSmokeParticles()
    {
        return exhaustionBigSmokeParticles;
    }

    public boolean disableMountedSpeedPotionBuff()
    {
        return disableMountedSpeedPotionBuff;
    }

    public boolean alternativeAgeMethod()
    {
        return alternativeBabyGrowth;
    }

    public Map<Material, Float> getBabyFoodMap()
    {
        return babyFoodMap;
    }

    public boolean teleportHorses()
    {
        return teleportHorses;
    }

    public boolean restrictLeads()
    {
        return restrictLeads;
    }

    public boolean allowFeeding()
    {
        return allowFeeding;
    }

    public Set<Material> getFoodItems()
    {
        return foodItems;
    }
}
