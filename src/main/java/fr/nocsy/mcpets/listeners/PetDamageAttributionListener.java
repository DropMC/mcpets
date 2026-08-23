package fr.nocsy.mcpets.listeners;

import fr.nocsy.mcpets.MCPets;
import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.utils.PetDamage;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Makes a pet's damage count as its owner's damage.
 * <p>
 * A pet is an extension of the player, so a mob it kills should drop its loot and experience, count towards the
 * owner's jobs and missions, and answer {@code getKiller()} with the owner. None of that happens on its own: the
 * damage comes from the pet entity, so the server credits the mob's death to a wolf nobody can see.
 * <p>
 * The event cannot be edited into shape, because the entity behind the damage is fixed once the event is built. The
 * hit is therefore cancelled and dealt again through a damage source that names the owner as the causing entity and
 * the pet as the direct one, which is exactly how vanilla models an arrow: the shooter gets the credit, the arrow
 * keeps the knockback.
 */
public class PetDamageAttributionListener implements Listener {

    /**
     * Runs after {@link PetPveListener}, which cancels at {@code LOWEST}, so anything a pet is not allowed to hurt
     * has already been dropped and is never re-dealt here.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void creditOwner(final EntityDamageByEntityEvent event) {
        // Damage already credited to a player needs no re-attribution. This also stops the hit dealt below from
        // coming back round for another pass, since it still names the pet as its direct entity.
        if (event.getDamageSource().getCausingEntity() instanceof Player) {
            return;
        }

        final Pet pet = PetDamage.behind(event);
        if (pet == null || pet.getOwner() == null || !(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }

        final Player owner = Bukkit.getPlayer(pet.getOwner());
        if (owner == null || owner.getWorld() != victim.getWorld()) {
            return;
        }

        final double amount = event.getDamage();
        event.setCancelled(true);

        // Dealt on the next tick rather than inline: the server is still inside this victim's damage handling, and a
        // second hit landed there is swallowed before it reaches the health calculation.
        Bukkit.getScheduler().runTask(MCPets.getInstance(), () -> {
            if (!victim.isValid() || !owner.isOnline()) {
                return;
            }
            // Pet skills are written to ignore invulnerability frames (pi=true in the MythicMobs skill files), and
            // going back through Bukkit would reinstate them, so a fast pet would silently lose most of its hits.
            victim.setNoDamageTicks(0);
            victim.damage(amount, owner);
        });
    }
}
