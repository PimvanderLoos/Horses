package nl.pim16aap2.horses;

import nl.pim16aap2.horses.listeners.HorseListener;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class Horses extends JavaPlugin
{
    private final HorseListener horseListener;
    private final Config config;

    public Horses()
    {
        config = new Config();
        horseListener = new HorseListener(config);
    }

    @Override
    public void onDisable()
    {
        Bukkit.getPluginManager().registerEvents(horseListener, this);
    }

    @Override
    public void onEnable()
    {
        HandlerList.unregisterAll(horseListener);
    }
}
