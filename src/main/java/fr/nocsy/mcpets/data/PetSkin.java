package fr.nocsy.mcpets.data;

import fr.nocsy.mcpets.MCPets;
import fr.nocsy.mcpets.utils.PDCTag;
import fr.nocsy.mcpets.data.config.FormatArg;
import fr.nocsy.mcpets.data.config.Language;
import fr.nocsy.mcpets.data.inventories.PetInventoryHolder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PetSkin {

    private static final Map<String, List<PetSkin>> petSkins = new ConcurrentHashMap<>();

    @Getter
    private String uuid;

    @Getter
    private Pet objectPet;
    @Getter
    private String mythicMobId;
    @Getter
    private ItemStack icon;
    @Getter
    private String permission;
    @Getter
    private String pathId;

    private PetSkin(String pathId, Pet objectPet, String mythicMobId, String permission) {
        this.uuid = UUID.randomUUID().toString();

        this.pathId = pathId;
        this.objectPet = objectPet;
        this.mythicMobId = mythicMobId;
        this.permission = permission;
        initIcon();
    }

    /**
     * Load the PetSkin object in the cache
     */
    public static void load(String pathId, Pet objectPet, String modelSkinId, String permission, ItemStack icon) {
        PetSkin petSkin = new PetSkin(pathId, objectPet, modelSkinId, permission);
        petSkin.setIcon(icon);

        petSkins.computeIfAbsent(objectPet.getId(), k -> new CopyOnWriteArrayList<>()).add(petSkin);
    }

    /**
     * Fetch the PetSkin from the icon
     */
    public static PetSkin fromIcon(ItemStack it) {
        if (it.hasItemMeta()) {
            String tagVal = PDCTag.get(it.getItemMeta());
            if (tagVal == null) return null;
            String[] code = tagVal.split(";");
            if (code.length > 0 && code[0].equals("MCPetsSkins")) {
                String petId = code[1];
                String skinUuid = code[2];
                List<PetSkin> skins = petSkins.get(petId);
                if (skins != null) {
                    Optional<PetSkin> opt = skins.stream().filter(petSkin -> petSkin.getUuid().equals(skinUuid)).findFirst();
                    return opt.orElse(null);
                }
            }
        }
        return null;
    }

    /**
     * Fetch all skins from the pet
     */
    public static List<PetSkin> getSkins(Pet pet) {
        final List<PetSkin> skins = petSkins.get(pet.getId());
        if (skins == null)
            return new ArrayList<>();
        return skins;
    }

    /**
     * Open the pet skins to the player
     */
    public static boolean openInventory(Player p, Pet pet) {
        if (pet == null)
            return false;

        List<PetSkin> skins = petSkins.get(pet.getId());
        if (skins == null || skins.isEmpty())
            return false;

        skins = skins.stream().filter(petSkin -> p.hasPermission(petSkin.getPermission())).toList();

        int invSize = Math.min(skins.size(), 54);
        while (invSize <= 0 || invSize % 9 != 0)
            invSize++;

        Inventory inventory = new PetInventoryHolder(invSize,
                Language.PET_SKINS_TITLE.getMessageFormatted(
                        new FormatArg("%pet%", pet.getIcon().getItemMeta().getDisplayName())),
                PetInventoryHolder.Type.PET_SKINS_MENU).getInventory();

        for (PetSkin petSkin : skins) {
            inventory.addItem(petSkin.getIcon());
        }

        p.openInventory(inventory);
        addMetada(p);
        return true;
    }

    /**
     * Add metadata to handle GUI
     */
    private static void addMetada(Player p) {
        p.setMetadata("MCPetsSkins", new FixedMetadataValue(MCPets.getInstance(), "opened"));
    }

    /**
     * Remove the metadata to handle GUI
     */
    public static void removeMetadata(Player p) {
        p.setMetadata("MCPetsSkins", new FixedMetadataValue(MCPets.getInstance(), null));
    }

    /**
     * Clear previous entries
     */
    public static void clearList(Pet pet) {
        if (pet.hasSkins()) {
            petSkins.put(pet.getId(), new CopyOnWriteArrayList<>());
        }
    }

    /**
     * Check if the player has the skins metadata
     */
    public static boolean hasMetadata(Player p) {
        return !p.getMetadata("MCPetsSkins").isEmpty() &&
                p.getMetadata("MCPetsSkins").getFirst() != null &&
                p.getMetadata("MCPetsSkins").getFirst().value() != null;
    }

    /**
     * Set the petSkin icon
     */
    private void setIcon(ItemStack icon) {
        if (icon != null) {
            this.icon = icon;
            prepareIcon();
        }
    }

    /**
     * Initialize the icon
     */
    private void initIcon() {
        icon = Items.UNKNOWN.getItem().clone();
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName("§6Skin §7: " + mythicMobId);
        ArrayList<String> lore = new ArrayList<>();
        lore.add("§7Click to apply that skin");
        meta.setLore(lore);
        icon.setItemMeta(meta);
        prepareIcon();
    }

    /**
     * Add the required localized name to the icon so we can identify it later on
     */
    private void prepareIcon() {
        ItemMeta meta = icon.getItemMeta();
        PDCTag.set(meta, "MCPetsSkins;" + objectPet.getId() + ";" + uuid);
        icon.setItemMeta(meta);
    }

    /**
     * Apply the skin to the pet
     */
    public boolean apply(Pet instancePet) {
        if (!instancePet.isStillHere()) return false;

        if (!instancePet.getId().equals(objectPet.getId())) return false;

        Location loc = instancePet.getActiveMob().getEntity().getBukkitEntity().getLocation();

        boolean hasRider = instancePet.hasMount(Bukkit.getPlayer(instancePet.getOwner()));
        instancePet.setActiveSkin(this);

        instancePet.despawn(PetDespawnReason.SKIN);

        // The new skin spawns where the old one stood; the rider climbs back on from their own thread.
        Bukkit.getRegionScheduler().runDelayed(MCPets.getInstance(), loc, t -> {
            instancePet.spawn(loc, false);
            if (hasRider) {
                final Player rider = Bukkit.getPlayer(instancePet.getOwner());
                if (rider != null) {
                    rider.getScheduler().runDelayed(MCPets.getInstance(),
                            t2 -> instancePet.setMount(Bukkit.getPlayer(instancePet.getOwner())), null, 2L);
                }
            }
        }, 2L);
        return true;
    }
}
