package nl.pim16aap2.horses;

import dagger.BindsInstance;
import dagger.Component;
import nl.pim16aap2.horses.commands.CommandListener;
import nl.pim16aap2.horses.listeners.HorseListener;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Singleton;

@SuppressWarnings("unused")
@Singleton
@Component(modules = {
    HorsesModule.class
})
public interface HorsesComponent
{
    @Component.Builder
    interface Builder
    {
        @BindsInstance
        Builder setPlugin(Horses javaPlugin);

        HorsesComponent build();
    }

    JavaPlugin getHorsesPlugin();

    Config getConfig();

    Communicator getCommunicator();

    HorseListener getHorseListener();

    CommandListener getCommandListener();

    HorseEditor getHorseEditor();
}
