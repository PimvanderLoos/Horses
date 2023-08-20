package nl.pim16aap2.horses.listeners;

import nl.pim16aap2.horses.Config;
import nl.pim16aap2.horses.util.Util;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.event.entity.EntityMountEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class PotionListener implements Listener
{
    private final Config config;

    @Inject PotionListener(Config config)
    {
        this.config = config;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMount(EntityMountEvent event)
    {
        if (!(event.getMount() instanceof AbstractHorse horse) ||
            !config.getMonitoredTypes().contains(horse.getType()) ||
            !(event.getEntity() instanceof Player player))
            return;

        player.removePotionEffect(PotionEffectType.SPEED);
        horse.removePotionEffect(PotionEffectType.SPEED);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPotionUsage(EntityPotionEffectEvent event)
    {
        if (event.getNewEffect() == null || event.getNewEffect().getType() != PotionEffectType.SPEED)
            return;

        if (event.getEntity() instanceof Player player && Util.getHorseRiddenByPlayer(player) != null ||
            config.getMonitoredTypes().contains(event.getEntity().getType()))
            event.setCancelled(true);
    }
}
