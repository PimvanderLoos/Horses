package nl.pim16aap2.horses.listeners;

import nl.pim16aap2.horses.Config;
import nl.pim16aap2.horses.Horses;
import nl.pim16aap2.horses.commands.CommandListener;
import nl.pim16aap2.horses.util.IReloadable;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ListenerManager implements IReloadable
{
    private final FeedListener feedListener;
    private final LeadRestrictionListener leadRestrictionListener;
    private final TeleportListener teleportListener;
    private final SelectorToolListener selectorToolListener;
    private final CommandListener commandListener;
    private final CommandListener.EditHorseTabComplete tabCompleter;
    private final Horses plugin;
    private final Config config;
    private final HorseListener horseListener;
    private final PotionListener potionListener;

    @Inject ListenerManager
        (
            FeedListener feedListener,
            LeadRestrictionListener leadRestrictionListener,
            TeleportListener teleportListener,
            SelectorToolListener selectorToolListener,
            CommandListener commandListener,
            CommandListener.EditHorseTabComplete tabCompleter,
            HorseListener horseListener,
            PotionListener potionListener,
            Config config,
            Horses plugin
        )
    {
        this.feedListener = feedListener;
        this.leadRestrictionListener = leadRestrictionListener;
        this.teleportListener = teleportListener;
        this.selectorToolListener = selectorToolListener;
        this.commandListener = commandListener;
        this.tabCompleter = tabCompleter;
        this.horseListener = horseListener;
        this.potionListener = potionListener;
        this.config = config;
        this.plugin = plugin;

        plugin.registerReloadable(this);
    }

    public void onEnable()
    {
        registerAll(
            feedListener,
            horseListener,
            selectorToolListener);
        if (config.disableMountedSpeedPotionBuff())
            Bukkit.getPluginManager().registerEvents(potionListener, plugin);
        if (config.teleportHorses())
            Bukkit.getPluginManager().registerEvents(teleportListener, plugin);
        if (config.restrictLeads())
            Bukkit.getPluginManager().registerEvents(leadRestrictionListener, plugin);

        initCommandListener();
    }

    public void onDisable()
    {
        unregisterAll(
            feedListener,
            leadRestrictionListener,
            horseListener,
            potionListener,
            teleportListener,
            selectorToolListener);
    }

    private void registerAll(Listener... listeners)
    {
        for (final var listener : listeners)
            Bukkit.getPluginManager().registerEvents(listener, plugin);
    }

    private void unregisterAll(Listener... listeners)
    {
        for (final var listener : listeners)
            HandlerList.unregisterAll(listener);
    }

    @Override
    public void reload()
    {
        onDisable();
        onEnable();
    }

    private void initCommandListener()
    {
        initCommands(
            "ReloadHorses",
            "GetHorseInfo");
        initCommand("EditHorse", tabCompleter);
    }

    private void initCommands(String... names)
    {
        for (final String name : names)
            initCommand(name, null);
    }

    private void initCommand(String name, @Nullable TabCompleter tabCompleter)
    {
        final @Nullable PluginCommand command = plugin.getCommand(name);
        if (command == null)
        {
            plugin.getLogger().severe("Failed to register command: '" + name + "'");
            return;
        }
        command.setExecutor(commandListener);
        if (tabCompleter != null)
            command.setTabCompleter(tabCompleter);
    }
}
