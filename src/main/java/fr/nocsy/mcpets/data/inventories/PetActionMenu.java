package fr.nocsy.mcpets.data.inventories;

import fr.nocsy.mcpets.data.Items;
import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.data.config.GlobalConfig;
import fr.nocsy.mcpets.data.config.Language;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * The trimmed interaction menu a player reaches from the command instead of the full pet menu:
 * back, skins, rename and the pet itself.
 */
public final class PetActionMenu {

    private static final int INV_SIZE = 9;

    private PetActionMenu() {}

    public static void open(final Player p, final Pet pet, final boolean isMount) {
        if (pet.getTamingProgress() < 1) {
            return;
        }

        pet.setOwner(p.getUniqueId());
        final String title = isMount
                ? Language.INVENTORY_MOUNTS_MENU_INTERACTIONS.getMessage()
                : Language.INVENTORY_PETS_MENU_INTERACTIONS.getMessage();
        final PetInventoryHolder.Type type = isMount
                ? PetInventoryHolder.Type.MOUNT_INTERACTION_MENU
                : PetInventoryHolder.Type.PET_INTERACTION_MENU;
        final Inventory inventory = new PetInventoryHolder(INV_SIZE, title, type).getInventory();

        inventory.setItem(0, isMount ? Items.MOUNTMENU.getItem() : Items.PETMENU.getItem());
        if (pet.hasSkins()) {
            inventory.setItem(2, Items.SKINS.getItem());
        }
        if (GlobalConfig.getInstance().isNameable()) {
            inventory.setItem(3, Items.RENAME.getItem());
        }
        inventory.setItem(4, pet.buildItem(Items.petInfo(pet), true));

        p.openInventory(inventory);
    }
}
