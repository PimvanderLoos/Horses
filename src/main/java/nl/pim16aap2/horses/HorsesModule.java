package nl.pim16aap2.horses;

import dagger.Binds;
import dagger.Module;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Singleton;

@SuppressWarnings("unused")
@Module
abstract class HorsesModule
{
    @Binds
    @Singleton
    abstract JavaPlugin getPlugin(Horses javaPlugin);
}
