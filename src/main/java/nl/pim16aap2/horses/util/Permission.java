package nl.pim16aap2.horses.util;

import org.bukkit.entity.Player;

public enum Permission
{
    USER_SEE_STAMINA_BAR("horses.user.see_stamina_bar"),
    USER_MAKE_GELDING("horses.admin.make_gelding"),
    ADMIN_FEED_BABY("horses.admin.feed_baby"),
    ADMIN_MAKE_GELDING("horses.admin.make_gelding"),
    ;

    private final String node;

    Permission(String node)
    {
        this.node = node;
    }

    public String getNode()
    {
        return node;
    }

    public boolean isSetFor(Player player)
    {
        return player.hasPermission(node);
    }
}
