package nl.pim16aap2.horses.util;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public enum Permission
{
    ADMIN_FEED_BABY("horses.admin.feed_baby"),
    ADMIN_MAKE_GELDING("horses.admin.make_gelding"),

    USER_SEE_STAMINA_BAR("horses.user.see_stamina_bar"),
    USER_MAKE_GELDING(ADMIN_MAKE_GELDING, "horses.admin.make_gelding"),
    ;

    private final @Nullable Permission adminVariant;
    private final String node;

    Permission(@Nullable Permission adminVariant, String node)
    {
        this.node = node;
        this.adminVariant = adminVariant;
    }

    Permission(String node)
    {
        this(null, node);
    }

    public @Nullable Permission getAdminVariant()
    {
        return adminVariant;
    }

    public String getNode()
    {
        return node;
    }

    public boolean isSetFor(Player player)
    {
        return player.hasPermission(node) ||
            (adminVariant != null && adminVariant.isSetFor(player));
    }
}
