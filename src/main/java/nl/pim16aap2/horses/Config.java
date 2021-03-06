package nl.pim16aap2.horses;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Config
{
    private static final String DEFAULT_GAITS = "0,25,35,50,75,100";

    private final JavaPlugin javaPlugin;

    private Material infoMaterial = Material.FEATHER;
    private Material whipMaterial = Material.BLAZE_ROD;
    private Gaits gaits;
    private int defaultGait = 100;
    private int resetGait = -1;
    private String[] genderNames;

    private final Path path;

    public Config(JavaPlugin javaPlugin)
    {
        this.javaPlugin = javaPlugin;
        gaits = parseGaits(DEFAULT_GAITS);
        this.path = javaPlugin.getDataFolder().toPath().resolve("config.yml");

        final HorseGender[] genders = HorseGender.values();
        genderNames = new String[genders.length];
        for (int idx = 0; idx < genders.length; ++idx)
            genderNames[idx] = Util.capitalizeFirstLetter(genders[idx].name());
    }

    public void reloadConfig()
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

        this.genderNames = readGenderNames(config);
    }

    private static String[] readGenderNames(FileConfiguration config)
    {
        final HorseGender[] genders = HorseGender.values();
        final String[] names = new String[genders.length];
        for (int idx = 0; idx < genders.length; ++idx)
        {
            final String baseName = Util.capitalizeFirstLetter(genders[idx].name());
            names[idx] = config.getString("name" + baseName, baseName);
        }
        return names;
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

    public String getGenderName(HorseGender gender)
    {
        return genderNames[gender.ordinal()];
    }
}
