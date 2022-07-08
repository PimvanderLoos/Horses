package nl.pim16aap2.horses;

import nl.pim16aap2.horses.commands.CommandListener;
import nl.pim16aap2.horses.listeners.HorseListener;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

@SuppressWarnings("unused")
public class Horses extends JavaPlugin
{
    public static final Set<EntityType> MONITORED_TYPES =
        EnumSet.of(EntityType.HORSE, EntityType.MULE, EntityType.DONKEY);

    private final HorseListener horseListener;
    private final Config config;
    private final HorseEditor horseEditor;
    private final Communicator communicator;
    private @Nullable CommandListener commandListener;

    public Horses()
    {
        config = new Config(this);
        horseEditor = new HorseEditor(this, config);
        communicator = new Communicator(config, horseEditor);
        horseListener = new HorseListener(this, config, horseEditor);
    }

    @Override
    public void onEnable()
    {
        config.reloadConfig();
        Bukkit.getPluginManager().registerEvents(horseListener, this);

        if (commandListener == null)
            initCommandListener();
    }

    public Communicator getCommunicator()
    {
        return communicator;
    }

    private void initCommandListener()
    {
        commandListener = new CommandListener(this);
        initCommand("ReloadHorses");
        initCommand("EditHorse", new CommandListener.EditHorseTabComplete(this));
    }

    private void initCommand(@SuppressWarnings("SameParameterValue") String name)
    {
        initCommand(name, null);
    }

    private void initCommand(String name, @Nullable TabCompleter tabCompleter)
    {
        final @Nullable PluginCommand command = getCommand(name);
        if (command == null)
        {
            getLogger().severe("Failed to register command: '" + name + "'");
            return;
        }
        command.setExecutor(commandListener);
        if (tabCompleter != null)
            command.setTabCompleter(tabCompleter);
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

    public HorseEditor getHorseEditor()
    {
        return horseEditor;
    }
}
