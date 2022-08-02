package nl.pim16aap2.horses.staminabar;

import nl.pim16aap2.horses.util.Localizer;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class StaminaBar implements IStaminaNotifier
{
    private final BossBar bossBar;

    StaminaBar(Localizer localizer, Player player, double percentage, boolean exhausted)
    {
        this.bossBar =
            Bukkit.createBossBar(localizer.get("notification.hud.stamina.title"), BarColor.GREEN, BarStyle.SOLID);
        setStamina(percentage, exhausted);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void notifyStaminaChange(Player player, double percentage, boolean exhausted)
    {
        setStamina(percentage, exhausted);
    }

    private void setStamina(double percentage, boolean exhausted)
    {
        this.bossBar.setProgress(percentage);
        this.updateColor(percentage, exhausted);
    }

    private void updateColor(double percentage, boolean exhausted)
    {
        final BarColor barColor;
        if (exhausted)
            barColor = BarColor.PINK;
        else if (percentage > 0.6)
            barColor = BarColor.GREEN;
        else if (percentage > 0.2)
            barColor = BarColor.YELLOW;
        else
            barColor = BarColor.RED;
        this.bossBar.setColor(barColor);
    }

    @Override
    public void kill()
    {
        this.bossBar.removeAll();
    }
}
