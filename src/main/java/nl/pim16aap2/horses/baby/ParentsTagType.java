package nl.pim16aap2.horses.baby;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ParentsTagType implements PersistentDataType<PersistentDataContainer, Parents>
{
    private final JavaPlugin javaPlugin;
    private final ParentKeys keys;

    ParentsTagType(JavaPlugin javaPlugin, ParentKeys keys)
    {
        this.javaPlugin = javaPlugin;
        this.keys = keys;
    }

    @Override
    public Class<PersistentDataContainer> getPrimitiveType()
    {
        return PersistentDataContainer.class;
    }

    @Override
    public Class<Parents> getComplexType()
    {
        return Parents.class;
    }

    @Override
    public PersistentDataContainer toPrimitive(Parents complex, PersistentDataAdapterContext context)
    {
        final PersistentDataContainer ret = context.newPersistentDataContainer();
        if (complex.father() != null)
        {
            ret.set(keys.keyFatherUUID(), PersistentDataType.STRING, complex.father().uuid().toString());
            ret.set(keys.keyFatherName(), PersistentDataType.STRING, complex.father().name());
        }
        if (complex.mother() != null)
        {
            ret.set(keys.keyMotherUUID(), PersistentDataType.STRING, complex.mother().uuid().toString());
            ret.set(keys.keyMotherName(), PersistentDataType.STRING, complex.mother().name());
        }
        return ret;
    }

    @Override
    public Parents fromPrimitive(PersistentDataContainer primitive, PersistentDataAdapterContext context)
    {
        final @Nullable Parent father = getParent(primitive, keys.keyFatherUUID(), keys.keyFatherName());
        final @Nullable Parent mother = getParent(primitive, keys.keyMotherUUID(), keys.keyMotherName());
        return new Parents(father, mother);
    }

    private @Nullable Parent getParent(PersistentDataContainer container, NamespacedKey uuidKey, NamespacedKey nameKey)
    {
        final @Nullable UUID uuid = parseUUID(container.get(uuidKey, PersistentDataType.STRING));
        final @Nullable String name = container.get(nameKey, PersistentDataType.STRING);
        return uuid != null && name != null ? new Parent(uuid, name) : null;
    }

    private @Nullable UUID parseUUID(@Nullable String input)
    {
        if (input == null)
            return null;
        try
        {
            return UUID.fromString(input);
        }
        catch (IllegalArgumentException e)
        {
            javaPlugin.getLogger().severe("Failed to parse UUID from input: '" + input + "'!");
            return null;
        }
    }
}
