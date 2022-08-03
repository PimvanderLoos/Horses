package nl.pim16aap2.horses.baby;


import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ParentFactory
{
    private final JavaPlugin javaPlugin;
    private final ParentKeys parentKeys;

    @Inject ParentFactory(JavaPlugin javaPlugin)
    {
        this.javaPlugin = javaPlugin;
        parentKeys = new ParentKeys(
            new NamespacedKey(javaPlugin, "fatherUUID"), new NamespacedKey(javaPlugin, "motherUUID"),
            new NamespacedKey(javaPlugin, "fatherName"), new NamespacedKey(javaPlugin, "motherName"));
    }

    public Parents of(@Nullable AbstractHorse father, @Nullable AbstractHorse mother)
    {
        return new Parents(father == null ? null : new Parent(father),
                           mother == null ? null : new Parent(mother));
    }

    public ParentsTagType tagType()
    {
        return new ParentsTagType(javaPlugin, parentKeys);
    }
}
