package fr.nocsy.mcpets.listeners;

import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.data.PetSkin;
import fr.nocsy.mcpets.data.config.Language;
import fr.nocsy.mcpets.data.inventories.PetInventoryHolder;
import fr.nocsy.mcpets.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class PetSkinsMenuListener implements Listener {

    @EventHandler
    public void click(final InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof final PetInventoryHolder holder)) {
            return;
        }

        if (holder.getType() != PetInventoryHolder.Type.PET_SKINS_MENU) {
            return;
        }

        if (!(e.getWhoClicked() instanceof final Player p)) {
            return;
        }

        if (!PetSkin.hasMetadata(p)) {
            return;
        }
        e.setCancelled(true);

        final ItemStack it = e.getCurrentItem();
        if (it == null || it.getType().isAir() || !it.hasItemMeta()) {
            return;
        }

        final PetSkin petSkin = PetSkin.fromIcon(it);
        if (petSkin == null) {
            return;
        }

        final Pet pet = Pet.fromOwner(p.getUniqueId());
        if (pet == null) {
            Language.REVOKED_BEFORE_CHANGES.sendMessage(p);
            p.closeInventory();
            return;
        }

        final Entity petEntity = pet.getEntityIfStillHere();
        if (petEntity != null && !Bukkit.isOwnedByCurrentRegion(petEntity)) {
            // The pet ticks on another region: the skin is swapped there and the result reported from there.
            p.closeInventory();
            Tasks.onEntity(petEntity, () -> {
                if (petSkin.apply(pet))
                    Language.SKIN_APPLIED.sendMessage(p);
                else
                    Language.SKIN_COULD_NOT_APPLY.sendMessage(p);
                pet.setActiveSkin(petSkin);
            }, () -> {
                Language.SKIN_COULD_NOT_APPLY.sendMessage(p);
                pet.setActiveSkin(petSkin);
            });
            return;
        }

        if (petSkin.apply(pet))
            Language.SKIN_APPLIED.sendMessage(p);
        else
            Language.SKIN_COULD_NOT_APPLY.sendMessage(p);

        pet.setActiveSkin(petSkin);
        p.closeInventory();
    }

    @EventHandler
    public void close(final InventoryCloseEvent e) {
        final Player p = (Player) e.getPlayer();
        if (PetSkin.hasMetadata(p)) {
            PetSkin.removeMetadata(p);
        }
    }
}
