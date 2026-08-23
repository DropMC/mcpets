package fr.nocsy.mcpets.listeners;

import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.utils.PetDamage;
import fr.nocsy.mcpets.utils.PetProtection;
import gg.dropmc.survival.core.util.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;

/**
 * Keeps pets out of everything this PvE server does not have: fighting players, and fighting what belongs to them.
 * <p>
 * The rule is enforced on the damage event rather than on MCPets' own {@code petDamage} mechanic because the pets
 * installed here attack through MythicMobs' native {@code damage} mechanic, which never reaches that code. Enforcing
 * it here also covers whatever a future pet's skill file decides to do.
 */
public class PetPveListener implements Listener {

    /**
     * Cancels any damage a pet deals to something it is not allowed to hurt, however it was dealt: a direct hit, a
     * projectile the pet fired, or an area effect the damage source attributes to it.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void petDamagesProtected(EntityDamageByEntityEvent event) {
        if (!isProtectedFromPets(event.getEntity()) || PetDamage.behind(event) == null) {
            return;
        }
        event.setDamage(0);
        event.setCancelled(true);
    }

    /**
     * Cancels damage a pet deals where its owner would not be allowed to deal it. The pets hit through
     * MythicMobs' native damage mechanic, which arrives as mob-on-mob damage that no region plugin guards,
     * so without this a pet kills the NPCs and animals at spawn that its owner cannot touch.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void petDamagesOutOfBounds(EntityDamageByEntityEvent event) {
        Pet pet = PetDamage.behind(event);
        if (pet == null || PetProtection.mayDamage(owner(pet), event.getEntity())) {
            return;
        }
        event.setDamage(0);
        event.setCancelled(true);
    }

    /**
     * Cancels any damage a player deals to a pet, their own included. The owner's hit is what opens the pet menu
     * (see {@link PetListener#interact}), and that handler runs after this one because it does not ignore cancelled
     * events, so the menu still opens while the pet takes nothing.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerDamagesPet(EntityDamageByEntityEvent event) {
        if (Pet.getFromEntity(event.getEntity()) == null || DamageUtil.attacker(event) == null) {
            return;
        }
        event.setDamage(0);
        event.setCancelled(true);
    }

    /**
     * Stops a pet from taking a protected entity as its target at all, so it does not chase what it can never hurt.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void petTargetsProtected(EntityTargetEvent event) {
        Pet pet = Pet.getFromEntity(event.getEntity());
        if (pet == null) {
            return;
        }
        if (isProtectedFromPets(event.getTarget()) || !PetProtection.mayDamage(owner(pet), event.getTarget())) {
            event.setCancelled(true);
        }
    }

    /** The pet's owner if they are online, which is who every protection question is asked about. */
    private static Player owner(Pet pet) {
        return pet.getOwner() == null ? null : Bukkit.getPlayer(pet.getOwner());
    }

    /**
     * What a pet is never allowed to hurt: a player, another pet, or anything a player has tamed. Other pets and
     * tamed animals belong to someone just as much as the player does, and an area skill that wipes out the wolves
     * standing next to its target is the same problem as hitting the player. Hostile and wild mobs are untouched, so
     * a pet still fights everything it is meant to fight.
     *
     * @param entity the damage or target victim, which the target event allows to be null
     */
    private static boolean isProtectedFromPets(Entity entity) {
        if (entity instanceof Player || Pet.getFromEntity(entity) != null) {
            return true;
        }
        return entity instanceof Tameable tameable && tameable.isTamed();
    }

}
