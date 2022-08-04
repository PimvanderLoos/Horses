package nl.pim16aap2.horses;

import dagger.BindsInstance;
import dagger.Component;
import nl.pim16aap2.horses.horseselector.HorseSelectorManager;
import nl.pim16aap2.horses.horsetracker.HorseTracker;
import nl.pim16aap2.horses.listeners.ListenerManager;
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

    HorseTracker getHorseTracker();

    Localizer getLocalizer();

    StaminaNotifierManager getStaminaNotifierManager();

    ListenerManager getListenerManager();

    HorseSelectorManager getHorseSelectorManager();
}
