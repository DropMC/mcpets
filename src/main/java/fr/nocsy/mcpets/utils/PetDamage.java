package fr.nocsy.mcpets.utils;

import fr.nocsy.mcpets.data.Pet;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Resolves which pet is really behind a damage event.
 */
public final class PetDamage {

    private PetDamage() {
    }

    /**
     * The pet responsible for the damage, or null if no pet is behind it. The damage source is asked before the
     * projectile because it is what attributes an explosion or a lingering cloud back to whoever created it.
     */
    public static Pet behind(final EntityDamageByEntityEvent event) {
        final Pet direct = Pet.getFromEntity(event.getDamager());
        if (direct != null) {
            return direct;
        }

        final Pet causing = Pet.getFromEntity(event.getDamageSource().getCausingEntity());
        if (causing != null) {
            return causing;
        }

        if (event.getDamager() instanceof Projectile projectile) {
            final ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Entity entity) {
                return Pet.getFromEntity(entity);
            }
        }
        return null;
    }
}
