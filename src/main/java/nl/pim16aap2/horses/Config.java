package nl.pim16aap2.horses;

import org.bukkit.Material;

public class Config
{
    private Material infoMaterial = Material.FEATHER;
    private Material whipMaterial = Material.BLAZE_ROD;

    public void reloadConfig()
    {
    }

    public Material getInfoMaterial()
    {
        return infoMaterial;
    }

    public Material getWhipMaterial()
    {
        return whipMaterial;
    }
}
