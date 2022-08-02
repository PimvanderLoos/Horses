package nl.pim16aap2.horses;

import dagger.BindsInstance;
import dagger.Component;
import nl.pim16aap2.horses.commands.CommandListener;
import nl.pim16aap2.horses.horsetracker.HorseTracker;
import nl.pim16aap2.horses.listeners.HorseListener;
import nl.pim16aap2.horses.staminabar.StaminaNotifierManager;
import nl.pim16aap2.horses.util.Localizer;

import javax.inject.Singleton;

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

    Config getConfig();

    HorseListener getHorseListener();

    CommandListener getCommandListener();

    HorseTracker getHorseTracker();

    Localizer getLocalizer();

    CommandListener.EditHorseTabComplete getEditHorseTabCompleter();

    StaminaNotifierManager getStaminaNotifierManager();
}
