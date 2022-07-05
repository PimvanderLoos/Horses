package nl.pim16aap2.horses.listeners;

import nl.pim16aap2.horses.Horses;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

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

        return false;
    }
}
