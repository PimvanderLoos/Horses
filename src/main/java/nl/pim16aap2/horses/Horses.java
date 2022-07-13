package nl.pim16aap2.horses;

import nl.pim16aap2.horses.staminabar.StaminaNotifierManager;
import nl.pim16aap2.horses.util.IReloadable;
import nl.pim16aap2.horses.util.Localizer;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class Horses extends JavaPlugin
{
    public static final Set<EntityType> MONITORED_TYPES =
        EnumSet.of(EntityType.HORSE, EntityType.MULE, EntityType.DONKEY);

    private final HorsesComponent horsesComponent;
    private final List<IReloadable> reloadables = new ArrayList<>();

    private final StaminaNotifierManager staminaNotifierManager = new StaminaNotifierManager();

    public Horses()
    {
        this.horsesComponent = DaggerHorsesComponent.builder().setPlugin(this).build();
        saveResource(Localizer.BASE_NAME + ".properties", false);
    }

    @Override
    public void onEnable()
    {
        horsesComponent.getConfig().reload();
        Bukkit.getPluginManager().registerEvents(horsesComponent.getHorseListener(), this);
        initCommandListener();

        horsesComponent.getHorseTracker().onEnable();
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
        initCommand("EditHorse", getHorsesComponent().getEditHorseTabCompleter());
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

    public void registerReloadable(IReloadable reloadable)
    {
        reloadables.add(reloadable);
    }

    public void reload()
    {
        reloadables.forEach(IReloadable::reload);
    }
}
