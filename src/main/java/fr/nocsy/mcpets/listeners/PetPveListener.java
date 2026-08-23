package fr.nocsy.mcpets.listeners;

import fr.nocsy.mcpets.data.Pet;
import gg.dropmc.survival.core.util.DamageUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Keeps pets out of player versus player combat, which this server does not have.
 * <p>
 * The rule is enforced on the damage event rather than on MCPets' own {@code petDamage} mechanic because the pets
 * installed here attack through MythicMobs' native {@code damage} mechanic, which never reaches that code. Enforcing
 * it here also covers whatever a future pet's skill file decides to do.
 */
public class PetPveListener implements Listener {

    /**
     * Cancels any damage a pet deals to a player, however it was dealt: a direct hit, a projectile the pet fired, or
     * an area effect the damage source attributes to it.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void petDamagesPlayer(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || petBehind(event) == null) {
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
     * Stops a pet from taking a player as its target at all, so it does not chase someone it can never hurt.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void petTargetsPlayer(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player) || Pet.getFromEntity(event.getEntity()) == null) {
            return;
        }
        event.setCancelled(true);
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
