package nl.pim16aap2.horses.baby;

import org.bukkit.NamespacedKey;

record ParentKeys(
    NamespacedKey keyFatherUUID, NamespacedKey keyMotherUUID, NamespacedKey keyFatherName, NamespacedKey keyMotherName)
{
}
