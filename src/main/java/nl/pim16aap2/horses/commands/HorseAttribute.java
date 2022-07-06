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
import java.util.UUID;
import java.util.stream.Stream;

enum HorseAttribute
{
    NAME("name")
        {
            @Override
            public boolean apply(Horses plugin, HorseEditor horseEditor, List<AbstractHorse> horses, String input)
            {
                for (final AbstractHorse horse : horses)
                    horseEditor.setName(horse, input);
                return true;
            }
        },
    GENDER("gender")
        {
            private final List<String> suggestions =
                Stream.of(HorseGender.values()).map(gender -> gender.name().toLowerCase(Locale.ROOT)).toList();

            @Override
            public boolean apply(Horses plugin, HorseEditor horseEditor, List<AbstractHorse> horses, String input)
            {
                try
                {
                    final HorseGender gender = HorseGender.valueOf(input.toUpperCase(Locale.ROOT));
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
    GAIT("gait")
        {
            @Override
            public boolean apply(Horses plugin, HorseEditor horseEditor, List<AbstractHorse> horses, String input)
            {
                try
                {
                    final int gait = Integer.parseInt(input);
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
    SPEED("speed")
        {
            @Override
            public boolean apply(Horses plugin, HorseEditor horseEditor, List<AbstractHorse> horses, String input)
            {
                try
                {
                    final double speed = Double.parseDouble(input);
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
    JUMP("jump")
        {
            @Override
            public boolean apply(Horses plugin, HorseEditor horseEditor, List<AbstractHorse> horses, String input)
            {
                try
                {
                    final double jumpStrength = Double.parseDouble(input);
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
    HEALTH("health")
        {
            @Override
            public boolean apply(Horses plugin, HorseEditor horseEditor, List<AbstractHorse> horses, String input)
            {
                try
                {
                    final double health = Math.max(0, Double.parseDouble(input));
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
    OWNER("owner")
        {
            @Override
            public boolean apply(Horses plugin, HorseEditor horseEditor, List<AbstractHorse> horses, String input)
            {
                @Nullable OfflinePlayer player;
                try
                {
                    player = Bukkit.getOfflinePlayer(UUID.fromString(input));
                }
                catch (IllegalArgumentException exception)
                {
                    player = Bukkit.getPlayer(input);
                }
                if (player == null)
                    return false;
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
            public String getErrorString(String input)
            {
                return "Could not find player '" + input + "'! Are they online? Or try their UUID!";
            }
        },
    ;

    private static final Map<String, HorseAttribute> nameMapper;

    static
    {
        final HorseAttribute[] values = values();
        nameMapper = new HashMap<>(values.length);
        for (final HorseAttribute attribute : values())
            nameMapper.put(attribute.name.toLowerCase(Locale.ROOT), attribute);
    }

    private final String name;

    HorseAttribute(String name)
    {
        this.name = name;
    }

    public static @Nullable HorseAttribute getAttribute(String input)
    {
        return nameMapper.get(input.toLowerCase(Locale.ROOT));
    }

    public String getName()
    {
        return name;
    }

    public abstract boolean apply(Horses plugin, HorseEditor horseEditor, List<AbstractHorse> horses, String input);

    public String getErrorString(String input)
    {
        return "Failed to parse input '" + input + "'";
    }

    public List<String> getSuggestions(Horses plugin)
    {
        return Collections.emptyList();
    }
}
