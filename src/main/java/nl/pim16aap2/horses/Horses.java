package nl.pim16aap2.horses;

import nl.pim16aap2.horses.util.IReloadable;
import nl.pim16aap2.horses.util.Localizer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class Horses extends JavaPlugin
{
    private static @Nullable Horses instance;

    private final HorsesComponent horsesComponent;
    private final List<IReloadable> reloadables = new ArrayList<>();

    public Horses()
    {
        instance = this;
        this.horsesComponent = DaggerHorsesComponent.builder().setPlugin(this).build();
        saveResource(Localizer.BASE_NAME + ".properties", false);
    }

    public static Horses instance()
    {
        //noinspection ConstantConditions
        return Objects.requireNonNull(instance);
    }

    @Override
    public void onEnable()
    {
        horsesComponent.getConfig().reload();
        horsesComponent.getListenerManager().onEnable();
        horsesComponent.getHorseTracker().onEnable();
    }

    @Override
    public void onDisable()
    {
        horsesComponent.getListenerManager().onDisable();
        horsesComponent.getStaminaNotifierManager().removeAll();
        horsesComponent.getHorseSelectorManager().reload();
    }

    public HorsesComponent getHorsesComponent()
    {
        return horsesComponent;
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
