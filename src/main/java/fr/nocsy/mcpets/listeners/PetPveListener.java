package fr.nocsy.mcpets.listeners;

import fr.nocsy.mcpets.data.Pet;
import gg.dropmc.survival.core.util.DamageUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.projectiles.ProjectileSource;

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
        if (!isProtectedFromPets(event.getEntity()) || petBehind(event) == null) {
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
        if (!isProtectedFromPets(event.getTarget()) || Pet.getFromEntity(event.getEntity()) == null) {
            return;
        }
        event.setCancelled(true);
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

    /**
     * The pet responsible for the damage, or null if no pet is behind it. The damage source is asked first because it
     * is what attributes an explosion or a lingering cloud back to whoever created it.
     */
    private static Pet petBehind(EntityDamageByEntityEvent event) {
        Pet direct = Pet.getFromEntity(event.getDamager());
        if (direct != null) {
            return direct;
        }

        Pet causing = Pet.getFromEntity(event.getDamageSource().getCausingEntity());
        if (causing != null) {
            return causing;
        }

        if (event.getDamager() instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Entity entity) {
                return Pet.getFromEntity(entity);
            }
        }
        return null;
    }
}
