package nl.pim16aap2.horses.util;

import org.bukkit.entity.Player;

public enum Permission
{
    USER_STAMINA_BAR("horses.user.staminabar"),
    ADMIN_FEED_BABY("horses.admin.feed_baby");

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