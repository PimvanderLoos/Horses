package nl.pim16aap2.horses;

import nl.pim16aap2.horses.listeners.CommandListener;
import nl.pim16aap2.horses.listeners.HorseListener;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class Horses extends JavaPlugin
{
    private final HorseListener horseListener;
    private final Config config;
    private @Nullable CommandListener commandListener;

    public Horses()
    {
        config = new Config(this);
        final HorseEditor horseEditor = new HorseEditor(this, config);
        horseListener = new HorseListener(config, horseEditor);
    }

    @Override
    public void onEnable()
    {
        config.reloadConfig();
        Bukkit.getPluginManager().registerEvents(horseListener, this);

        if (commandListener == null)
            initCommandListener();
    }

    private void initCommandListener()
    {
        commandListener = new CommandListener(this);
        initCommand("reloadhorses");
    }

    private void initCommand(@SuppressWarnings("SameParameterValue") String name)
    {
        final @Nullable PluginCommand command = getCommand(name);
        if (command == null)
        {
            getLogger().severe("Failed to register command: '" + name + "'");
            return;
        }
        command.setExecutor(commandListener);
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
