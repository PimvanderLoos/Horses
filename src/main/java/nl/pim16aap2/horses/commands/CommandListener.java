package nl.pim16aap2.horses.commands;

import nl.pim16aap2.horses.Horses;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class CommandListener implements CommandExecutor
{
    private final Horses horses;

    public CommandListener(Horses horses)
    {
        this.horses = horses;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (command.getName().equalsIgnoreCase("ReloadHorses"))
        {
            horses.getHorsesConfig().reloadConfig();
            final String color = sender instanceof Player ? ChatColor.GREEN.toString() : "";
            sender.sendMessage(color + "Plugin has been reloaded!");
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
                player.sendMessage(ChatColor.RED + "Could not find attribute " + args[0]);
                return false;
            }

            final String permission = getAttributePermission(attribute.getName());
            if (!player.hasPermission(permission))
            {
                horses.getLogger()
                      .info("Player '" + player.getName() + "' does not have permission node '" + permission + "'!");
                player.sendMessage(ChatColor.RED + "You do not have permission to execute this command!");
                return true;
            }

            if (attribute.isParameterRequired() && value == null)
            {
                player.sendMessage(ChatColor.RED + "Required command value not provided!");
                return false;
            }

            final List<AbstractHorse> leadHorses = getHorsesLeadBy(player);
            if (leadHorses.isEmpty())
            {
                player.sendMessage(ChatColor.RED + "You are not currently leading any horses!");
                return true;
            }

            if (!attribute.apply(horses, horses.getHorseEditor(), leadHorses, value))
                player.sendMessage(ChatColor.RED + attribute.getErrorString(value));
            else
                player.sendMessage(ChatColor.GREEN + "Attribute '" +
                                       ChatColor.GOLD + attribute.getName() +
                                       ChatColor.GREEN + "' has been updated!");
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
