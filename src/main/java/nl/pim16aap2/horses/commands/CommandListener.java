package nl.pim16aap2.horses.commands;

import nl.pim16aap2.horses.Communicator;
import nl.pim16aap2.horses.Config;
import nl.pim16aap2.horses.HorseEditor;
import nl.pim16aap2.horses.Horses;
import nl.pim16aap2.horses.util.IReloadable;
import nl.pim16aap2.horses.util.Localizer;
import nl.pim16aap2.horses.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import static nl.pim16aap2.horses.commands.ModifiableAttribute.ExecutionResult;

@Singleton
public class CommandListener implements CommandExecutor
{
    private final Horses horses;
    private final HorseEditor horseEditor;
    private final Localizer localizer;
    private final AttributeMapper attributeMapper;
    private final Communicator communicator;
    private final Config config;

    @Inject CommandListener(
        Horses horses,
        HorseEditor horseEditor,
        Localizer localizer,
        AttributeMapper attributeMapper,
        Communicator communicator,
        Config config)
    {
        this.horses = horses;
        this.horseEditor = horseEditor;
        this.localizer = localizer;
        this.attributeMapper = attributeMapper;
        this.communicator = communicator;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (command.getName().equalsIgnoreCase("ReloadHorses"))
        {
            horses.reload();
            final String color = sender instanceof Player ? ChatColor.GREEN.toString() : "";
            sender.sendMessage(color + localizer.get("commands.success.plugin_reloaded"));
            return true;
        }

        if (command.getName().equalsIgnoreCase("GetHorseInfo"))
        {
            if (args.length != 1)
                return false;
            handleGetHorseInfo(sender, args[0]);
            return true;
        }

        if (command.getName().equalsIgnoreCase("ListHorseTypes"))
        {
            sendHorseTypes(sender);
            return true;
        }

        if (!(sender instanceof Player player))
        {
            sender.sendMessage("Only players can use the command " + command.getName());
            return true;
        }

        // /EditHorse <attribute> [value]
        if (command.getName().equalsIgnoreCase("EditHorse"))
        {
            if (args.length == 0 || args.length > 2)
                return false;

            final @Nullable String value = args.length < 2 ? null : args[1];
            final @Nullable ModifiableAttribute attribute = attributeMapper.getAttribute(args[0]);
            if (attribute == null)
            {
                player.sendMessage(ChatColor.RED + localizer.get("commands.error.attribute_not_found", args[0]));
                return false;
            }

            final String permission = getAttributePermission(attribute.getName());
            if (!player.hasPermission(permission))
            {
                horses.getLogger()
                      .info("Player '" + player.getName() + "' does not have permission node '" + permission + "'!");
                player.sendMessage(ChatColor.RED + localizer.get("commands.error.no_permission"));
                return true;
            }

            if (attribute.isParameterRequired() && value == null)
            {
                player.sendMessage(ChatColor.RED + localizer.get("commands.error.missing_required_value"));
                return false;
            }

            final List<AbstractHorse> leadHorses = Util.getLeadAndRiddenHorses(player);
            if (leadHorses.isEmpty())
            {
                player.sendMessage(ChatColor.RED + localizer.get("commands.error.no_horses_targeted"));
                return true;
            }

            final ExecutionResult result =
                attribute.apply(horses, horseEditor, sender, leadHorses, value, attributeMapper);
            if (result == ExecutionResult.SUCCESS)
                sendSuccessMessage(attributeMapper, localizer, player, attribute);
            else if (result == ExecutionResult.ERROR)
                player.sendMessage(ChatColor.RED + attribute.getErrorString(horses, value));
            return true;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private static int compareEntityTypes(EntityType a, EntityType b)
    {
        final @Nullable String aName = a.getName();
        if (aName == null)
            return -1;
        final @Nullable String bName = b.getName();
        if (bName == null)
            return 1;
        return aName.compareTo(bName);
    }

    private void sendHorseTypes(CommandSender sender)
    {
        final boolean isPlayer = sender instanceof Player;
        final String blue = isPlayer ? ChatColor.AQUA.toString() : "";
        final String green = isPlayer ? ChatColor.GREEN.toString() : "";
        final String red = isPlayer ? ChatColor.RED.toString() : "";

        final String prefixPositive = green + " + ";
        final String prefixNegative = red + " - ";

        final StringBuilder typeList = new StringBuilder();
        Util.getSupportedEntityTypes().stream()
            .sorted(CommandListener::compareEntityTypes)
            .forEach(
                type ->
                {
                    final boolean isMonitored = config.getMonitoredTypes().contains(type);
                    final String prefix = isMonitored ? prefixPositive : prefixNegative;

                    //noinspection deprecation
                    final String typeName = Objects.requireNonNull(
                        type.getName(), "Entity type name must not be null (type: " + type + ")");

                    typeList.append('\n').append(prefix).append(typeName).append(blue);
                });

        sender.sendMessage(blue + localizer.get("commands.success.monitored_types", typeList.toString()));
    }

    private void handleGetHorseInfo(CommandSender sender, String input)
    {
        final @Nullable UUID uuid = Util.parseUUID(input);
        if (uuid == null)
        {
            sender.sendMessage(localizer.get("commands.error.invalid_attribute_value", input));
            return;
        }

        final @Nullable Entity entity = Bukkit.getEntity(uuid);
        if (!(entity instanceof AbstractHorse horse) ||
            !config.getMonitoredTypes().contains(horse.getType()))
        {
            sender.sendMessage(localizer.get("commands.error.no_horses_found", uuid.toString()));
            return;
        }

        communicator.printInfo(sender, horse);
    }

    static String getAttributePermission(String attributeName)
    {
        return "horses.editattribute." + attributeName;
    }

    static void sendSuccessMessage(
        AttributeMapper attributeMapper, Localizer localizer, Player player, ModifiableAttribute attribute)
    {
        final String attributeName =
            ChatColor.GOLD + attributeMapper.getLocalizedName(attribute) + ChatColor.GREEN;
        player.sendMessage(
            ChatColor.GREEN + localizer.get("commands.success.attribute_updated", attributeName));
    }

    @Singleton
    public static final class EditHorseTabComplete implements TabCompleter, IReloadable
    {
        private final Horses plugin;
        private final AttributeMapper attributeMapper;
        private List<String> attributeNames = Collections.emptyList();

        @Inject EditHorseTabComplete(Horses plugin, AttributeMapper attributeMapper)
        {
            plugin.registerReloadable(this);
            this.plugin = plugin;
            this.attributeMapper = attributeMapper;
            reload();
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
        {
            if (args.length == 0)
                return attributeNames;

            if (args.length == 1)
            {
                var stream = attributeNames.stream().filter(name -> name.startsWith(args[0].toLowerCase(Locale.ROOT)));
                if (sender instanceof Player player)
                    stream = stream.filter(name -> player.hasPermission(getAttributePermission(name)));
                return stream.toList();
            }

            final @Nullable ModifiableAttribute attribute = attributeMapper.getAttribute(args[0]);
            final List<String> ret = attribute == null ? Collections.emptyList() : attribute.getSuggestions(plugin);

            if (args.length == 2)
                return ret.stream().filter(name -> name.startsWith(args[1].toLowerCase(Locale.ROOT))).toList();
            return ret;
        }

        @Override
        public void reload()
        {
            this.attributeNames = attributeMapper.getNames();
        }
    }
}
