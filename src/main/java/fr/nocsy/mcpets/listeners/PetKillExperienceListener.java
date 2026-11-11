package fr.nocsy.mcpets.listeners;

import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.data.config.GlobalConfig;
import fr.nocsy.mcpets.utils.Tasks;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Gives the active pet experience for every hostile mob its owner kills while it is out, the pet's own kills
 * included (they are credited to the owner, see {@link PetDamageAttributionListener}).
 * <p>
 * The skill cast was the only source before, a couple of points every 15 to 30 seconds of cooldown, so a pet
 * took many hours of non-stop fighting to level. Spawner and spawn egg mobs give nothing: a mob farm would
 * otherwise max a pet in minutes.
 */
public class PetKillExperienceListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKill(final EntityDeathEvent event) {
        final LivingEntity victim = event.getEntity();
        final Player killer = victim.getKiller();
        if (killer == null || !(victim instanceof Enemy) || Pet.getFromEntity(victim) != null) return;

        final SpawnReason reason = victim.getEntitySpawnReason();
        if (reason == SpawnReason.SPAWNER || reason == SpawnReason.SPAWNER_EGG) return;

        final double amount = GlobalConfig.getInstance().getExperiencePerKill();
        if (amount <= 0) return;

        final Pet pet = Pet.fromOwner(killer.getUniqueId());
        if (pet == null || !pet.isStillHere() || pet.getPetStats() == null
                || pet.getPetLevels() == null || pet.getPetLevels().size() < 2) return;

        Tasks.forPet(pet, () -> pet.getPetStats().addExperience(amount));
    }
}
