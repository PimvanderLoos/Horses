package nl.pim16aap2.horses;

import nl.pim16aap2.horses.commands.CommandListener;
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

    private final HorsesComponent horsesComponent;

    public Horses()
    {
        this.horsesComponent = DaggerHorsesComponent.builder().setPlugin(this).build();
    }

    @Override
    public void onEnable()
    {
        horsesComponent.getConfig().reloadConfig();
        Bukkit.getPluginManager().registerEvents(horsesComponent.getHorseListener(), this);
        initCommandListener();
    }

    @Override
    public void onDisable()
    {
        HandlerList.unregisterAll(horsesComponent.getHorseListener());
    }

    public HorsesComponent getHorsesComponent()
    {
        return horsesComponent;
    }

    private void initCommandListener()
    {
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
        command.setExecutor(horsesComponent.getCommandListener());
        if (tabCompleter != null)
            command.setTabCompleter(tabCompleter);
    }
}
