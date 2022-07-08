package nl.pim16aap2.horses;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Communicator
{
    private final Config config;
    private final HorseEditor horseEditor;

    public Communicator(Config config, HorseEditor horseEditor)
    {
        this.config = config;
        this.horseEditor = horseEditor;
    }

    public void communicateSpeedChange(Player player, int newSpeed)
    {
        player.spigot().sendMessage(
            ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Speed: " + newSpeed));
    }

    public void printInfo(Player player, AbstractHorse horse)
    {
        horseEditor.ensureHorseManaged(horse);

        //noinspection deprecation
        final String msg = ChatColor.DARK_GRAY + ">>>>>>--------------------------<<<<<<<\n"
            + addInfo("Name", Objects.requireNonNullElse(horse.getCustomName(), horse.getType().getName()))
            + addInfo("Gender", config.getGenderName(horseEditor.getGender(horse)))
            + addInfo("Gait", horseEditor.getGait(horse))
            + addInfo("Speed", String.format("%.2f", horseEditor.getBaseSpeed(horse) * 43.17f))
            + addInfo("Jump", String.format("%.2f", horse.getJumpStrength()))
            + addInfo("Health", String.format("%.0f", horse.getHealth()))
            + addInfo("Owner", getOwnerName(horse))
            + ChatColor.DARK_GRAY + ">>>>>>--------------------------<<<<<<<\n";

        player.sendMessage(msg);
    }

    private String getOwnerName(AbstractHorse horse)
    {
        final @Nullable AnimalTamer owner = horse.getOwner();
        return owner == null ? "Unowned" : Objects.toString(owner.getName());
    }

    private String addInfo(String name, Object value)
    {
        return ChatColor.GOLD + name + ": " + ChatColor.GRAY + value + "\n";
    }
}
