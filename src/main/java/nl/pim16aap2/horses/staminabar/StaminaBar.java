package nl.pim16aap2.horses.staminabar;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class StaminaBar implements IStaminaNotifier
{
    private final BossBar bossBar;

    StaminaBar(Player player)
    {
        this.bossBar = Bukkit.createBossBar("Stamina", BarColor.GREEN, BarStyle.SOLID);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void notifyStaminaChange(Player player, double percentage, boolean exhausted)
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
