package nl.pim16aap2.horses.baby;

import org.bukkit.Bukkit;
import org.bukkit.entity.AbstractHorse;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class Parent
{
    private final UUID uuid;
    private final String name;

    public Parent(UUID uuid, String name)
    {
        this.uuid = uuid;
        this.name = name;
    }

    public Parent(AbstractHorse horse)
    {
        this(horse.getUniqueId(), horse.getName());
    }

    @Nullable AbstractHorse getEntity()
    {
        return Bukkit.getEntity(uuid) instanceof AbstractHorse horse ? horse : null;
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

    public boolean isAlive()
    {
        final @Nullable AbstractHorse horse = getEntity();
        return horse != null && horse.isValid();
    }

    public UUID uuid()
    {
        return uuid;
    }

    public String name()
    {
        return name;
    }
}
