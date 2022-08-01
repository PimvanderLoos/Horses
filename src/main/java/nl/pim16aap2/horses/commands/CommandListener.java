package nl.pim16aap2.horses.commands;

import nl.pim16aap2.horses.HorseEditor;
import nl.pim16aap2.horses.Horses;
import nl.pim16aap2.horses.util.Localizer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Singleton
public class CommandListener implements CommandExecutor
{
    private final Horses horses;
    private final HorseEditor horseEditor;
    private final Localizer localizer;

    @Inject
    public CommandListener(Horses horses, HorseEditor horseEditor, Localizer localizer)
    {
        this.horses = horses;
        this.horseEditor = horseEditor;
        this.localizer = localizer;
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
            final @Nullable ModifiableAttribute attribute = ModifiableAttribute.getAttribute(args[0]);
            if (attribute == null)
            {
                player.sendMessage(ChatColor.RED + localizer.get("commands.error.attribute_not_found"), args[0]);
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

            final List<AbstractHorse> leadHorses = getHorsesLeadBy(player);
            if (leadHorses.isEmpty())
            {
                player.sendMessage(ChatColor.RED + localizer.get("commands.error.no_horses_targeted"));
                return true;
            }

            if (!attribute.apply(horses, horseEditor, leadHorses, value))
                player.sendMessage(ChatColor.RED + attribute.getErrorString(horses, value));
            else
            {
                final String attributeName = localizer.get(
                    ChatColor.GOLD + "horse.attribute." + attribute.getName() + ChatColor.GREEN);
                player.sendMessage(
                    ChatColor.GREEN + localizer.get("commands.success.attribute_updated"), attributeName);
            }
            return true;
        }
        return false;
    }

    static String getAttributePermission(String attributeName)
    {
        return "horses.editattribute." + attributeName;
    }

    private List<AbstractHorse> getHorsesLeadBy(Player player)
    {
        var ret = player.getNearbyEntities(10, 10, 10).stream()
                        .filter(entity -> Horses.MONITORED_TYPES.contains(entity.getType()))
                        .map(AbstractHorse.class::cast)
                        .filter(AbstractHorse::isLeashed)
                        .filter(horse -> player.equals(horse.getLeashHolder()))
                        .toList();

        if (player.getVehicle() != null &&
            Horses.MONITORED_TYPES.contains(player.getVehicle().getType()) &&
            player.getVehicle() instanceof AbstractHorse horse)
        {
            ret = new ArrayList<>(ret);
            ret.add(horse);
        }
        return ret;
    }

    public static final class EditHorseTabComplete implements TabCompleter
    {
        private final Horses plugin;
        private final List<String> attributeNames;

        public EditHorseTabComplete(Horses plugin)
        {
            this.plugin = plugin;
            attributeNames = Stream.of(ModifiableAttribute.values()).map(ModifiableAttribute::getName).toList();
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

            final @Nullable ModifiableAttribute attribute = ModifiableAttribute.getAttribute(args[0]);
            final List<String> ret = attribute == null ? Collections.emptyList() : attribute.getSuggestions(plugin);

            if (args.length == 2)
                return ret.stream().filter(name -> name.startsWith(args[1].toLowerCase(Locale.ROOT))).toList();
            return ret;
        }
    }
}
