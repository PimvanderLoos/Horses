package nl.pim16aap2.horses;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import nl.pim16aap2.horses.baby.Parent;
import nl.pim16aap2.horses.baby.Parents;
import nl.pim16aap2.horses.staminabar.IStaminaNotifier;
import nl.pim16aap2.horses.util.Localizer;
import nl.pim16aap2.horses.util.Permission;
import nl.pim16aap2.horses.util.Util;
import org.bukkit.command.CommandSender;
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
            new TextComponent(org.bukkit.ChatColor.RED + localizer.get("notification.hud.speed", newSpeed)));
    }

    public void printInfo(CommandSender commandSender, AbstractHorse horse)
    {
        if (!Permission.USER_SEE_INFO_MENU.isSetFor(commandSender))
        {
            commandSender.sendMessage(ChatColor.RED + localizer.get("notification.error.generic_no_permission"));
            return;
        }

        if (commandSender instanceof Player player &&
            !Util.checkPlayerAccess(player, horse, Permission.ADMIN_SEE_INFO_MENU))
        {
            player.sendMessage(ChatColor.RED + localizer.get("notification.error.generic_no_access"));
            return;
        }

        horseEditor.ensureHorseManaged(horse);

        final ComponentBuilder builder = new ComponentBuilder()
            .color(ChatColor.DARK_GRAY).append(">>>>>>--------------------------<<<<<<<").append("\n");

        //noinspection deprecation
        appendInfo(builder, localizer.get("horse.attribute.name"),
                   Objects.requireNonNullElse(horse.getCustomName(), horse.getType().getName()));

        appendInfo(builder, localizer.get("horse.attribute.gender"),
                   localizer.get("horse.gender." + horseEditor.getGender(horse).name().toLowerCase(Locale.ROOT)));

        appendInfo(builder, localizer.get("horse.attribute.gait"), horseEditor.getGait(horse));

        appendInfo(builder, localizer.get("horse.attribute.speed"),
                   String.format("%.2f", horseEditor.getBaseSpeed(horse) * 43.17f));

        appendInfo(builder, localizer.get("horse.attribute.jump"),
                   String.format(Locale.ROOT, "%.2f", horse.getJumpStrength()));

        appendInfo(builder, localizer.get("horse.attribute.health"),
                   String.format(Locale.ROOT, "%d", (int) Math.floor(horse.getHealth() / 2.0F)));

        appendInfo(builder, localizer.get("horse.attribute.exhausted"),
                   localizer.get(horseEditor.isExhausted(horse) ? "horse.attribute.exhausted.true" :
                                 "horse.attribute.exhausted.false"));

        appendInfo(builder, localizer.get("horse.attribute.owner"), getOwnerName(horse));

        if (!horse.isAdult())
            appendInfo(builder, localizer.get("horse.attribute.grow_progress"), Util.formatBabyGrowthPercentage(horse));


        final Parents parents = horseEditor.getParents(horse);
        appendParentInfo(builder, "horse.attribute.father", parents.father());
        appendParentInfo(builder, "horse.attribute.mother", parents.mother());

        // Remove last newline.
        builder.removeComponent(builder.getCursor());
        commandSender.spigot().sendMessage(builder.create());
    }

    private String getOwnerName(AbstractHorse horse)
    {
        final @Nullable AnimalTamer owner = horse.getOwner();
        return owner == null ? localizer.get("horse.attribute.owner.unowned") : Objects.toString(owner.getName());
    }

    private void appendInfo(ComponentBuilder builder, String name, Object value)
    {
        builder.color(ChatColor.GOLD)
               .append(name).append(": ")
               .color(ChatColor.GRAY)
               .append(Objects.toString(value))
               .append("\n");
    }

    private void appendParentInfo(ComponentBuilder builder, String localizationKey, @Nullable Parent parent)
    {
        if (parent == null)
        {
            appendInfo(builder, localizer.get(localizationKey), localizer.get("horse.parents.unknown"));
            return;
        }

        final boolean isAlive = parent.isAlive();

        final TextComponent clickableName = new TextComponent(
            new ComponentBuilder().color(isAlive ? ChatColor.AQUA : ChatColor.GRAY).italic(isAlive).underlined(isAlive)
                                  .append(parent.getUpToDateName()).create());

        if (isAlive)
        {
            clickableName.setHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                               new Text(localizer.get("message.instruction.click_to_see_parent"))));
            clickableName.setClickEvent(
                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gethorseinfo " + parent.uuid()));
        }

        builder.color(ChatColor.GOLD)
               .append(localizer.get(localizationKey)).append(": ")
               .append(clickableName)
               .italic(false).underlined(false);

        if (!isAlive)
            builder.append(" ")
                   .color(ChatColor.RED)
                   .append("(").append(localizer.get("horse.status.deceased")).append(")");

        builder.append("\n");
    }

    @Override
    public void notifyStaminaChange(Player player, double percentage, boolean exhausted)
    {
        player.spigot().sendMessage(
            ChatMessageType.CHAT,
            new TextComponent(org.bukkit.ChatColor.GREEN +
                                  localizer.get("notification.hud.stamina.message",
                                                String.format(" %3.2f%%", (100 * percentage)))));
    }
}
