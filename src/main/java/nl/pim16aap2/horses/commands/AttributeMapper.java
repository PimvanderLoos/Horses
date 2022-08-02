package nl.pim16aap2.horses.commands;

import nl.pim16aap2.horses.Horses;
import nl.pim16aap2.horses.util.IReloadable;
import nl.pim16aap2.horses.util.Localizer;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Singleton
class AttributeMapper implements IReloadable
{
    private final Localizer localizer;

    private Map<String, ModifiableAttribute> attributeMap = Collections.emptyMap();

    @Inject AttributeMapper(Horses plugin, Localizer localizer)
    {
        plugin.registerReloadable(this);
        this.localizer = localizer;
        reload();
    }

    @Override
    public void reload()
    {
        attributeMap = new HashMap<>();
        for (final ModifiableAttribute attribute : ModifiableAttribute.values())
            attributeMap.put(getLocalizedName(attribute).replaceAll(" ", "_"), attribute);
    }

    public @Nullable ModifiableAttribute getAttribute(@Nullable String input)
    {
        if (input == null)
            return null;
        return attributeMap.get(input);
    }

    public String getLocalizedName(ModifiableAttribute attribute)
    {
        return localizer.get("horse.attribute." + attribute.getName().toLowerCase(Locale.ROOT));
    }

    public List<String> getNames()
    {
        return new ArrayList<>(attributeMap.keySet());
    }
}
