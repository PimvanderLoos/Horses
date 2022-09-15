package nl.pim16aap2.horses.baby;

import org.bukkit.Bukkit;
import org.bukkit.entity.AbstractHorse;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record Parent(UUID uuid, String name)
{
    private static final UUID EMPTY_UUID = new UUID(0L, 0L);

    public Parent(AbstractHorse horse)
    {
        this(horse.getUniqueId(), horse.getName());
    }

    @Nullable AbstractHorse getEntity()
    {
        return (!uuid.equals(EMPTY_UUID)) && Bukkit.getEntity(uuid) instanceof AbstractHorse horse ? horse : null;
    }

    /**
     * @return The most up-to-date name of the entity. If the entity is alive, their current name will be used. If not,
     * their defined name will be used as fallback instead.
     */
    public String getUpToDateName()
    {
        final @Nullable AbstractHorse entity = getEntity();
        if (entity == null)
            return name;
        return entity.getName();
    }

    /**
     * Checks if the current name set for this parent is the same as the current name of the entity.
     *
     * @return True if the current name of this parent is up-to-date with the entity it represents.
     */
    public boolean isNameUpToDate()
    {
        final @Nullable AbstractHorse entity = getEntity();
        if (entity == null)
            return true;
        return name.equals(entity.getName());
    }

    public boolean isAlive()
    {
        final @Nullable AbstractHorse horse = getEntity();
        return horse != null && horse.isValid();
    }
}
