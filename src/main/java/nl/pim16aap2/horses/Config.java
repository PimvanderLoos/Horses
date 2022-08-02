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
import java.util.List;

@Singleton
public class Config implements IReloadable
{
    private static final String DEFAULT_GAITS = "0,25,35,50,75,100";

    private final JavaPlugin javaPlugin;

    private Material infoMaterial = Material.FEATHER;
    private Material whipMaterial = Material.BLAZE_ROD;
    private Gaits gaits;
    private int defaultGait = 100;
    private int resetGait = -1;
    private int exhaustionPenalty = 35;
    private int energyDrainTime = 5;
    private int energyRecoveryTime = 7;

    private int exhaustionSmokeParticles = 8;
    private int exhaustionBigSmokeParticles = 2;

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
        this.whipMaterial = readMaterial(config, "whipMaterial", Material.BLAZE_ROD);

        this.gaits = parseGaits(config.getString("gaits", DEFAULT_GAITS));
        this.defaultGait = parseInt(config, "defaultGait", 100);
        this.resetGait = parseInt(config, "resetGait", 100);

        this.exhaustionPenalty = parseInt(config, "exhaustionPenalty", 25);
        this.energyDrainTime = parseInt(config, "energyDrainTime", 5);
        this.energyRecoveryTime = parseInt(config, "energyRecoveryTime", 7);

        this.exhaustionSmokeParticles = parseInt(config, "exhaustionSmokeParticles", 8);
        this.exhaustionBigSmokeParticles = parseInt(config, "exhaustionBigSmokeParticles", 2);
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

    public int getExhaustionSmokeParticles()
    {
        return exhaustionSmokeParticles;
    }

    public int getExhaustionBigSmokeParticles()
    {
        return exhaustionBigSmokeParticles;
    }
}
