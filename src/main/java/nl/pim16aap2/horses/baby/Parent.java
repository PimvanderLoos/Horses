package nl.pim16aap2.horses.baby;

import org.bukkit.Bukkit;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record Parent(UUID uuid, String name)
{
    public Parent(AbstractHorse horse)
    {
        this(horse.getUniqueId(), horse.getName());
    }

    @Nullable AbstractHorse getEntity()
    {
        final @Nullable Entity entity = Bukkit.getEntity(uuid);
        return entity instanceof AbstractHorse horse ? horse : null;
    }
}
