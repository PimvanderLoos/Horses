package nl.pim16aap2.horses.util;

import nl.pim16aap2.horses.Horses;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

@Singleton
public class Localizer implements IReloadable
{
    public static final String BASE_NAME = "horses_messages";

    private final Horses plugin;
    private @Nullable URLClassLoader classLoader;

    @Inject
    public Localizer(Horses plugin)
    {
        this.plugin = plugin;
        classLoader = newClassLoader();
        plugin.registerReloadable(this);
    }

    @Override
    public void reload()
    {
        if (classLoader != null)
        {
            try
            {
                classLoader.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        classLoader = newClassLoader();
    }

    private @Nullable URLClassLoader newClassLoader()
    {
        try
        {
            return new URLClassLoader(new URL[]{plugin.getDataFolder().toURI().toURL()});
        }
        catch (MalformedURLException e)
        {
            plugin.getLogger().severe("Failed to initialize localization!");
            e.printStackTrace();
            return null;
        }
    }

    public String get(String key)
    {
        if (classLoader == null)
            return "Failed to initialize localization! Please contact a server administrator!";
        try
        {
            return ResourceBundle.getBundle(BASE_NAME, Locale.getDefault(), classLoader).getString(key);
        }
        catch (Exception e)
        {
            plugin.getLogger().severe("Failed to find translation for key: '" + key + "'");
            e.printStackTrace();
            return "Localization error! Please contact a server administrator!";
        }
    }

    public String get(String key, Object... objects)
    {
        try
        {
            return MessageFormat.format(get(key), objects);
        }
        catch (Exception e)
        {
            plugin.getLogger().severe(
                "Failed to find translation for key: '" + key + "' and inputs: " + Arrays.toString(objects));
            e.printStackTrace();
            return "Localization error! Please contact a server administrator!";
        }
    }
}
