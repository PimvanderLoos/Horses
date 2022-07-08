package nl.pim16aap2.horses.commands;

import nl.pim16aap2.horses.HorseEditor;
import nl.pim16aap2.horses.HorseGender;
import nl.pim16aap2.horses.Horses;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

enum ModifiableAttribute
{
    NAME("name", false)
        {
            @Override
            public boolean apply(
                Horses plugin, HorseEditor horseEditor, List<AbstractHorse> horses, @Nullable String input)
            {
                for (final AbstractHorse horse : horses)
                    horseEditor.setName(horse, input);
                return true;
            }
        },
    GENDER("gender", true)
        {
            private final List<String> suggestions =
                Stream.of(HorseGender.values()).map(gender -> gender.name().toLowerCase(Locale.ROOT)).toList();

            @Override
            public boolean apply(
                Horses plugin, HorseEditor horseEditor, List<AbstractHorse> horses, @Nullable String input)
            {
                try
                {
                    final HorseGender gender = HorseGender.valueOf(
                        Objects.requireNonNull(input).toUpperCase(Locale.ROOT));
                    for (final AbstractHorse horse : horses)
                        horseEditor.setGender(horse, gender);
                    return true;
                }
                catch (IllegalArgumentException exception)
                {
                    return false;
                }
            }

            @Override
            public List<String> getSuggestions(Horses plugin)
            {
                return suggestions;
            }
        },
    GAIT("gait", true)
        {
            @Override
            public boolean apply(
                Horses plugin, HorseEditor horseEditor, List<AbstractHorse> horses, @Nullable String input)
            {
                try
                {
                    final int gait = Integer.parseInt(Objects.requireNonNull(input));
                    for (final AbstractHorse horse : horses)
                        horseEditor.setGait(horse, gait);
                    return true;
                }
                catch (NumberFormatException exception)
                {
                    return false;
                }
            }

            @Override
            public List<String> getSuggestions(Horses plugin)
            {
                return plugin.getHorsesConfig().getGaits().gaitsAsList().stream().map(Object::toString).toList();
            }
        },
    SPEED("speed", true)
        {
            @Override
            public boolean apply(
                Horses plugin, HorseEditor horseEditor, List<AbstractHorse> horses, @Nullable String input)
            {
                try
                {
                    final double speed = Double.parseDouble(Objects.requireNonNull(input));
                    for (final AbstractHorse horse : horses)
                        horseEditor.setBaseSpeed(horse, speed);
                    return true;
                }
                catch (NumberFormatException exception)
                {
                    return false;
                }
            }
        },
    JUMP("jump", true)
        {
            @Override
            public boolean apply(
                Horses plugin, HorseEditor horseEditor, List<AbstractHorse> horses, @Nullable String input)
            {
                try
                {
                    final double jumpStrength = Double.parseDouble(Objects.requireNonNull(input));
                    for (final AbstractHorse horse : horses)
                        horse.setJumpStrength(jumpStrength);
                    return true;
                }
                catch (NumberFormatException exception)
                {
                    return false;
                }
            }
        },
    HEALTH("health", true)
        {
            @Override
            public boolean apply(
                Horses plugin, HorseEditor horseEditor, List<AbstractHorse> horses, @Nullable String input)
            {
                try
                {
                    final double health = Math.max(0, Double.parseDouble(Objects.requireNonNull(input)));
                    for (final AbstractHorse horse : horses)
                    {
                        horseEditor.setMaxHealth(horse, health);
                        horse.setHealth(health);
                    }
                    return true;
                }
                catch (NumberFormatException exception)
                {
                    return false;
                }
            }
        },
    OWNER("owner", false)
        {
            private @Nullable OfflinePlayer parsePlayer(String input)
            {
                try
                {
                    return Bukkit.getOfflinePlayer(UUID.fromString(input));
                }
                catch (IllegalArgumentException exception)
                {
                    return Bukkit.getPlayer(input);
                }
            }

            @Override
            public boolean apply(
                Horses plugin, HorseEditor horseEditor, List<AbstractHorse> horses, @Nullable String input)
            {
                final @Nullable OfflinePlayer player = input == null ? null : parsePlayer(input);
                for (final AbstractHorse horse : horses)
                    horse.setOwner(player);
                return true;
            }

            @Override
            public List<String> getSuggestions(Horses plugin)
            {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            }

            @Override
            public String getErrorString(@Nullable String input)
            {
                return "Could not find player '" + input + "'! Are they online? Or try their UUID!";
            }
        },
    ;

    private static final Map<String, ModifiableAttribute> NAME_MAPPER;

    static
    {
        final ModifiableAttribute[] values = values();
        NAME_MAPPER = new HashMap<>(values.length);
        for (final ModifiableAttribute attribute : values())
            NAME_MAPPER.put(attribute.name.toLowerCase(Locale.ROOT), attribute);
    }

    private final String name;
    private final boolean parameterRequired;

    ModifiableAttribute(String name, boolean parameterRequired)
    {
        this.name = name;
        this.parameterRequired = parameterRequired;
    }

    public static @Nullable ModifiableAttribute getAttribute(String input)
    {
        return NAME_MAPPER.get(input.toLowerCase(Locale.ROOT));
    }

    public String getName()
    {
        return name;
    }

    public boolean isParameterRequired()
    {
        return parameterRequired;
    }

    public abstract boolean apply(
        Horses plugin, HorseEditor horseEditor, List<AbstractHorse> horses, @Nullable String input);

    public String getErrorString(@Nullable String input)
    {
        return "Failed to parse input '" + input + "'";
    }

    public List<String> getSuggestions(Horses plugin)
    {
        return Collections.emptyList();
    }
}
