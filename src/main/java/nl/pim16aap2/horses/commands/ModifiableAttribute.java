package nl.pim16aap2.horses.commands;

import nl.pim16aap2.horses.HorseEditor;
import nl.pim16aap2.horses.HorseGender;
import nl.pim16aap2.horses.Horses;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

enum ModifiableAttribute
{
    NAME("name", false)
        {
            @Override
            public boolean apply(
                Horses plugin, HorseEditor horseEditor, CommandSender commandSender, List<AbstractHorse> horses,
                @Nullable String input)
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
                Horses plugin, HorseEditor horseEditor, CommandSender commandSender, List<AbstractHorse> horses,
                @Nullable String input)
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
                Horses plugin, HorseEditor horseEditor, CommandSender commandSender, List<AbstractHorse> horses,
                @Nullable String input)
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
                return plugin.getHorsesComponent().getConfig()
                             .getGaits().gaitsAsList().stream().map(Object::toString).toList();
            }
        },
    SPEED("speed", true)
        {
            @Override
            public boolean apply(
                Horses plugin, HorseEditor horseEditor, CommandSender commandSender, List<AbstractHorse> horses,
                @Nullable String input)
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
                Horses plugin, HorseEditor horseEditor, CommandSender commandSender, List<AbstractHorse> horses,
                @Nullable String input)
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
                Horses plugin, HorseEditor horseEditor, CommandSender commandSender, List<AbstractHorse> horses,
                @Nullable String input)
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
                Horses plugin, HorseEditor horseEditor, CommandSender commandSender, List<AbstractHorse> horses,
                @Nullable String input)
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
            public String getErrorString(Horses plugin, @Nullable String input)
            {
                return plugin.getHorsesComponent().getLocalizer()
                             .get("commands.error.player_not_found", input == null ? "NULL" : input);
            }
        },
    STYLE("style", true)
        {
            private static final List<String> SUGGESTIONS =
                Arrays.stream(Horse.Style.values()).map(style -> style.name().toLowerCase(Locale.ROOT)).toList();

            @Override
            public boolean apply(
                Horses plugin, HorseEditor horseEditor, CommandSender commandSender, List<AbstractHorse> horses,
                @Nullable String input)
            {
                try
                {
                    final Horse.Style style = Horse.Style.valueOf(
                        Objects.requireNonNull(input).toUpperCase(Locale.ROOT));
                    for (final AbstractHorse abstractHorse : horses)
                        if (abstractHorse instanceof Horse horse)
                            horse.setStyle(style);
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
                return SUGGESTIONS;
            }

            @Override
            public String getErrorString(Horses plugin, @Nullable String input)
            {
                return plugin.getHorsesComponent().getLocalizer()
                             .get("commands.error.style_not_found", input == null ? "NULL" : input);
            }
        },
    COLOR("color", true)
        {
            private static final List<String> SUGGESTIONS =
                Arrays.stream(Horse.Color.values()).map(color -> color.name().toLowerCase(Locale.ROOT)).toList();

            @Override
            public boolean apply(
                Horses plugin, HorseEditor horseEditor, CommandSender commandSender, List<AbstractHorse> horses,
                @Nullable String input)
            {
                try
                {
                    final Horse.Color color = Horse.Color.valueOf(
                        Objects.requireNonNull(input).toUpperCase(Locale.ROOT));
                    for (final AbstractHorse abstractHorse : horses)
                        if (abstractHorse instanceof Horse horse)
                            horse.setColor(color);
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
                return SUGGESTIONS;
            }

            @Override
            public String getErrorString(Horses plugin, @Nullable String input)
            {
                return plugin.getHorsesComponent().getLocalizer()
                             .get("commands.error.color_not_found", input == null ? "NULL" : input);
            }
        },
    FATHER("father", false)
        {
            @Override
            public boolean apply(
                Horses plugin, HorseEditor horseEditor, CommandSender commandSender, List<AbstractHorse> horses,
                @Nullable String input)
            {
                if (input != null)
                {
                    for (final var horse : horses)
                        horseEditor.setFather(horse, input);
                    return true;
                }

                if (!(commandSender instanceof Player player))
                {
                    plugin.getLogger().severe("Only players can use the selection process!");
                    return true;
                }

                plugin.getHorsesComponent().getHorseSelectorManager().newWaiter(
                    player, selected ->
                    {
                        for (final var horse : horses)
                            horseEditor.setFather(horse, selected);
                    });

                return true;
            }
        },
    MOTHER("mother", false)
        {
            @Override
            public boolean apply(
                Horses plugin, HorseEditor horseEditor, CommandSender commandSender, List<AbstractHorse> horses,
                @Nullable String input)
            {
                if (input != null)
                {
                    for (final var horse : horses)
                        horseEditor.setMother(horse, input);
                    return true;
                }

                if (!(commandSender instanceof Player player))
                {
                    plugin.getLogger().severe("Only players can use the selection process!");
                    return true;
                }

                plugin.getHorsesComponent().getHorseSelectorManager().newWaiter(
                    player, selected ->
                    {
                        for (final var horse : horses)
                            horseEditor.setMother(horse, selected);
                    });
                return true;
            }
        },
    ;

    private final String name;
    private final boolean parameterRequired;

    ModifiableAttribute(String name, boolean parameterRequired)
    {
        this.name = name;
        this.parameterRequired = parameterRequired;
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
        Horses plugin, HorseEditor horseEditor, CommandSender commandSender, List<AbstractHorse> horses,
        @Nullable String input);

    public String getErrorString(Horses plugin, @Nullable String input)
    {
        return plugin.getHorsesComponent().getLocalizer()
                     .get("commands.error.invalid_attribute_value", input == null ? "NULL" : input);
    }

    public List<String> getSuggestions(Horses plugin)
    {
        return Collections.emptyList();
    }
}
