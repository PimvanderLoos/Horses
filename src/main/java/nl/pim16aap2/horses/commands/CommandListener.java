package nl.pim16aap2.horses.commands;

import nl.pim16aap2.horses.Horses;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

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
            return true;
        }

        if (!(sender instanceof Player player))
        {
            sender.sendMessage("Only players can use the command " + command.getName());
            return true;
        }

        // /EditHorse <attribute> <value>
        if (command.getName().equalsIgnoreCase("EditHorse"))
        {
            if (args.length != 2)
                return false;

            final @Nullable HorseAttribute attribute = HorseAttribute.getAttribute(args[0]);
            if (attribute == null)
            {
                player.sendMessage("Could not find attribute " + args[0]);
                return false;
            }

            final List<AbstractHorse> leadHorses = getHorsesLeadBy(player);
            if (leadHorses.isEmpty())
            {
                player.sendMessage("You are not currently leading any horses!");
                return true;
            }

            if (!attribute.apply(horses, horses.getHorseEditor(), leadHorses, args[1]))
                player.sendMessage(attribute.getErrorString(args[1]));
            return true;
        }
        return false;
    }

    private List<AbstractHorse> getHorsesLeadBy(Player player)
    {
        return player.getNearbyEntities(10, 10, 10).stream()
                     .filter(entity -> Horses.MONITORED_TYPES.contains(entity.getType()))
                     .map(AbstractHorse.class::cast)
                     .filter(horse -> horse.getLeashHolder().equals(player))
                     .toList();
    }

    public static final class EditHorseTabComplete implements TabCompleter
    {
        private final Horses plugin;
        private final List<String> attributeNames;

        public EditHorseTabComplete(Horses plugin)
        {
            this.plugin = plugin;
            attributeNames = Stream.of(HorseAttribute.values()).map(HorseAttribute::getName).toList();
        }

        @Override
        public @Nullable List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
        {
            if (args.length == 0)
                return attributeNames;

            if (args.length == 1)
                return attributeNames.stream()
                                     .filter(name -> name.startsWith(args[0].toLowerCase(Locale.ROOT))).toList();

            final @Nullable HorseAttribute attribute = HorseAttribute.getAttribute(args[0]);
            final List<String> ret = attribute == null ? Collections.emptyList() : attribute.getSuggestions(plugin);

            if (args.length == 2)
                return ret.stream().filter(name -> name.startsWith(args[1].toLowerCase(Locale.ROOT))).toList();
            return ret;
        }
    }
}
