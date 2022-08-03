package nl.pim16aap2.horses;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import nl.pim16aap2.horses.baby.Parents;
import nl.pim16aap2.horses.staminabar.IStaminaNotifier;
import nl.pim16aap2.horses.util.Localizer;
import nl.pim16aap2.horses.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Locale;
import java.util.Objects;

@Singleton
public class Communicator implements IStaminaNotifier
{
    private final HorseEditor horseEditor;
    private final Localizer localizer;

    @Inject
    public Communicator(HorseEditor horseEditor, Localizer localizer)
    {
        this.horseEditor = horseEditor;
        this.localizer = localizer;
    }

    public void communicateSpeedChange(Player player, int newSpeed)
    {
        player.spigot().sendMessage(
            ChatMessageType.ACTION_BAR,
            new TextComponent(ChatColor.RED + localizer.get("notification.hud.speed", newSpeed)));
    }

    public void printInfo(Player player, AbstractHorse horse)
    {
        horseEditor.ensureHorseManaged(horse);

        //noinspection deprecation
        String msg = ChatColor.DARK_GRAY + ">>>>>>--------------------------<<<<<<<\n"
            + addInfo(localizer.get("horse.attribute.name"),
                      Objects.requireNonNullElse(horse.getCustomName(), horse.getType().getName()))
            + addInfo(localizer.get("horse.attribute.gender"),
                      localizer.get("horse.gender." + horseEditor.getGender(horse).name().toLowerCase(Locale.ROOT)))
            + addInfo(localizer.get("horse.attribute.gait"), horseEditor.getGait(horse))
            + addInfo(localizer.get("horse.attribute.speed"),
                      String.format("%.2f", horseEditor.getBaseSpeed(horse) * 43.17f))
            + addInfo(localizer.get("horse.attribute.jump"),
                      String.format(Locale.ROOT, "%.2f", horse.getJumpStrength()))
            + addInfo(localizer.get("horse.attribute.health"), String.format(Locale.ROOT, "%.0f", horse.getHealth()))
            + addInfo(localizer.get("horse.attribute.exhausted"),
                      localizer.get(horseEditor.isExhausted(horse) ? "horse.attribute.exhausted.true" :
                                    "horse.attribute.exhausted.false"))
            + addInfo(localizer.get("horse.attribute.owner"), getOwnerName(horse));

        if (!horse.isAdult())
            msg += addInfo(localizer.get("horse.attribute.grow_progress"), Util.formatBabyGrowthPercentage(horse));

        final Parents parents = horseEditor.getParents(horse);
        if (parents.father() != null)
            msg += addInfo(localizer.get("horse.attribute.father"), (parents.father().toString()));
        if (parents.mother() != null)
            msg += addInfo(localizer.get("horse.attribute.father"), (parents.mother().toString()));

        msg += ChatColor.DARK_GRAY + ">>>>>>--------------------------<<<<<<<\n";

        player.sendMessage(msg);
    }

    private String getOwnerName(AbstractHorse horse)
    {
        final @Nullable AnimalTamer owner = horse.getOwner();
        return owner == null ? localizer.get("horse.attribute.owner.unowned") : Objects.toString(owner.getName());
    }

    private String addInfo(String name, Object value)
    {
        return ChatColor.GOLD + name + ": " + ChatColor.GRAY + value + "\n";
    }

    @Override
    public void notifyStaminaChange(Player player, double percentage, boolean exhausted)
    {
        player.spigot().sendMessage(
            ChatMessageType.CHAT,
            new TextComponent(ChatColor.GREEN + localizer.get("notification.hud.stamina.message",
                                                              String.format(" %3.2f%%", (100 * percentage)))));
    }
}
