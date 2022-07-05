package nl.pim16aap2.horses;

import nl.pim16aap2.horses.listeners.HorseListener;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public class Horses extends JavaPlugin
{
    private final HorseListener horseListener;
    private final Config config;

    public Horses()
    {
        config = new Config(this);
        horseListener = new HorseListener(config);
    }

    @Override
    public void onEnable()
    {
        config.reloadConfig();
        Bukkit.getPluginManager().registerEvents(horseListener, this);
    }

    @Override
    public void onDisable()
    {
        HandlerList.unregisterAll(horseListener);
    }

    public Config getHorsesConfig()
    {
        return config;
    }
}
