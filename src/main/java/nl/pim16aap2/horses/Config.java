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

    private final Path path;

    public Config(JavaPlugin javaPlugin)
    {
        this.javaPlugin = javaPlugin;
        gaits = parseGaits(DEFAULT_GAITS);
        this.path = javaPlugin.getDataFolder().toPath().resolve("config.yml");
    }

    public void reloadConfig()
    {
        javaPlugin.getLogger().info("(Re)Loading config!");
        ensureFileExists();
        javaPlugin.reloadConfig();
        setValues(javaPlugin.getConfig());
    }

    private void setValues(FileConfiguration config)
    {
        this.infoMaterial = readMaterial(config, "infoMaterial", Material.FEATHER);
        this.whipMaterial = readMaterial(config, "whipMaterial", Material.BLAZE_ROD);

        this.gaits = parseGaits(config.getString("gaits", DEFAULT_GAITS));
    }

    private Gaits parseGaits(String line)
    {
        final String[] parts = line.split(",");
        final List<Integer> values = new ArrayList<>();
        for (int idx = 0; idx < parts.length; ++idx)
        {
            try
            {
                values.add(Integer.parseInt(parts[idx]));
            }
            catch (NumberFormatException e)
            {
                javaPlugin.getLogger().severe("Failed to parse number '" + parts[idx] + "'");
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
}
