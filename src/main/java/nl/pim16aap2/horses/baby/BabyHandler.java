package nl.pim16aap2.horses.baby;

import nl.pim16aap2.horses.Config;
import nl.pim16aap2.horses.HorseEditor;
import org.bukkit.entity.AbstractHorse;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BabyHandler
{
    private final HorseEditor horseEditor;
    private final Config config;

    @Inject BabyHandler(HorseEditor horseEditor, Config config)
    {
        this.horseEditor = horseEditor;
        this.config = config;
    }

    /**
     * Handles a new baby being born.
     *
     * @param horseA
     *     The first parent.
     * @param horseB
     *     The second parent.
     * @param child
     *     The child that would be born if the action is allowed.
     * @return True if the new baby is allowed to be born, otherwise false.
     */
    public boolean newBaby(AbstractHorse horseA, AbstractHorse horseB, AbstractHorse child)
    {
        final boolean canBreed = horseEditor.canBreed(horseA, horseB);
        if (!canBreed)
            return false;

        horseEditor.ensureHorseManaged(child);

        if (config.alternativeAgeMethod())
            child.setAgeLock(true);

        return true;
    }
}
