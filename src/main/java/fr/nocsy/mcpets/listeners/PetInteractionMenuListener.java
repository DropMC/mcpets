package fr.nocsy.mcpets.listeners;

import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.Getter;

import org.jetbrains.annotations.NotNull;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.inventory.InventoryClickEvent;

import fr.nocsy.mcpets.MCPets;
import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.data.Items;
import fr.nocsy.mcpets.PPermission;
import fr.nocsy.mcpets.utils.Utils;
import fr.nocsy.mcpets.utils.PDCTag;
import fr.nocsy.mcpets.data.PetSkin;
import fr.nocsy.mcpets.data.config.Language;
import fr.nocsy.mcpets.data.PetDespawnReason;
import fr.nocsy.mcpets.data.config.FormatArg;
import fr.nocsy.mcpets.data.inventories.MountMenu;
import fr.nocsy.mcpets.data.inventories.PetInventory;
import fr.nocsy.mcpets.data.inventories.PetInventoryHolder;

public class PetInteractionMenuListener implements Listener {

    /** Answers the pets menu summon sound a fifth lower, so calling and storing read as one pair. */
    private static final Sound STORE_SOUND = Sound.ENTITY_ENDERMAN_TELEPORT;

    @Getter
    private static final List<UUID> waitingForAnswer = new CopyOnWriteArrayList<>();

    public static void changeName(@NotNull final Player p) {
        if (!waitingForAnswer.contains(p.getUniqueId())) {
            waitingForAnswer.add(p.getUniqueId());
        }
        Language.TYPE_NAME_IN_CHAT.sendMessage(p);
        Language.IF_WISH_TO_REMOVE_NAME.sendMessageFormatted(p, new FormatArg("%tag%", Language.TAG_TO_REMOVE_NAME.getMessage()));
    }

    public static void mount(@NotNull final Player p, final Pet pet) {
        if (p.isInsideVehicle()) {
            Language.ALREADY_INSIDE_VEHICULE.sendMessage(p);
        } else if (!pet.setMount(p)) {
            Language.NOT_MOUNTABLE.sendMessage(p);
        }
    }

    public static void inventory(final Player p, final Pet pet) {
        final PetInventory inventory = PetInventory.get(pet);
        if (inventory != null) inventory.open(p);
    }

    public static void skins(final Player p, final Pet pet) {
        new BukkitRunnable() {
            @Override
            public void run() {
                PetSkin.openInventory(p, pet);
            }
        }.runTaskLater(MCPets.getInstance(), 2L);
    }

    public static void revoke(final Player p, @NotNull final Pet pet) {
        pet.despawn(PetDespawnReason.REVOKE);
        Language.REVOKED.sendMessage(p);
        p.playSound(p.getLocation(), STORE_SOUND, 0.7F, 0.8F);
    }

    @EventHandler
    public void click(final InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof final PetInventoryHolder holder)) {
            return;
        }

        if (holder.getType() == PetInventoryHolder.Type.MOUNT_INTERACTION_MENU) {
            e.setCancelled(true);

            if (!(e.getWhoClicked() instanceof final Player p)) {
                return;
            }

            if (e.getClickedInventory() == null) {
                return;
            }

            final Pet pet = Optional.ofNullable(Pet.getFromLastInteractedWith(p)).orElse(Pet.getFromLastOpInteractedWith(p));
            if (pet == null || !pet.isStillHere()) {
                p.closeInventory();
                Language.REVOKED_BEFORE_CHANGES.sendMessage(p);
                return;
            }

            if (e.getSlot() == 4) {
                revoke(p, pet);
                p.closeInventory();
                return;
            }

            final ItemStack it = e.getCurrentItem();
            if (it != null && it.hasItemMeta() && it.getItemMeta().hasDisplayName()) {

                final String localizedName = PDCTag.get(it.getItemMeta());
                if (localizedName == null) return;

                if (localizedName.equals(Items.MOUNTMENU.getLocalizedName())) {
                    openBackMountMenu(p);
                    return;
                }

                if (localizedName.equals(Items.MOUNT.getLocalizedName())) {
                    mount(p, pet);
                } else if (localizedName.equals(Items.RENAME.getLocalizedName())) {
                    changeName(p);
                } else if (localizedName.equals(Items.INVENTORY.getLocalizedName())) {
                    inventory(p, pet);
                } else if (localizedName.equals(Items.SKINS.getLocalizedName())) {
                    skins(p, pet);
                }
                p.closeInventory();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void chat(final AsyncPlayerChatEvent e) {
        final Player p = e.getPlayer();

        if (waitingForAnswer.contains(p.getUniqueId())) {
            waitingForAnswer.remove(p.getUniqueId());
            e.setCancelled(true);

            String name = e.getMessage().replace("'", "");
            name = name.replace(";;", ";").replace(";;;", ";");

            final String blackListedWord = Utils.isInBlackList(name);
            if (blackListedWord != null) {
                Language.BLACKLISTED_WORD.sendMessageFormatted(p, new FormatArg("%word%", blackListedWord));
                return;
            }

            final Pet pet = Optional.ofNullable(Pet.getFromLastInteractedWith(p)).orElse(Pet.getFromLastOpInteractedWith(p));

            if (pet != null && pet.isStillHere()) {
                boolean stripColor = !p.hasPermission(PPermission.COLOR.getPermission());

                if (name.isEmpty()) {
                    Language.NICKNAME_NOT_CHANGED.sendMessage(p);
                    return;
                }
                pet.setDisplayName(name, true, stripColor);

                Language.NICKNAME_CHANGED_SUCCESSFULY.sendMessage(p);
            } else {
                Language.REVOKED_BEFORE_CHANGES.sendMessage(p);
            }
        }
    }

    private void openBackMountMenu(final Player p) {
        new MountMenu(p, 0).open(p);
    }
}
