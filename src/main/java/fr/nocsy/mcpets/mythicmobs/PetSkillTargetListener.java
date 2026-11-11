package fr.nocsy.mcpets.mythicmobs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.utils.PetProtection;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.events.MythicTargetEvent;

/**
 * Drops every player but the owner from the targets of a skill a pet casts, and everything the owner could not hit
 * where it stands (the villagers and animals inside the spawn safe zone).
 *
 * <p>Cancelling the damage a pet deals to a player is not enough: the pets attack through skills that also throw,
 * pull, stun, set on fire and hand out wither, and every one of those runs on the skill's target list whatever the
 * damage event decides afterwards. The same goes for a region: {@link fr.nocsy.mcpets.listeners.PetPveListener}
 * cancels the damage at spawn, but the throw and the stun still landed on the NPCs and animals there. The pets' own
 * skill files carry the filter that keeps a player off that list, and
 * this listener is what makes the rule hold for a pack nobody has audited yet.</p>
 *
 * <p>Only a skill the pet itself casts is filtered. A pet's skill file routinely sudoes its owner as the caster to
 * read their aim, message them and hang the attack cooldown on them, and those targets have to survive. The one
 * consequence is that a skill cast that way is not covered here: the recharge feedback of the Cubee pets, cast by the
 * owner, is kept off players by its own target conditions.</p>
 */
public class PetSkillTargetListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void petSkillTargets(final MythicTargetEvent event) {
        final SkillMetadata skill = event.getSkillMetadata();
        if (!skill.hasEntityTargets()) {
            return;
        }

        final SkillCaster caster = skill.getCaster();
        if (caster == null || caster.getEntity() == null) {
            return;
        }

        final Pet pet = Pet.getFromEntity(BukkitAdapter.adapt(caster.getEntity()));
        if (pet == null) {
            return;
        }

        final Player owner = PetProtection.owner(pet);
        final Entity self = BukkitAdapter.adapt(caster.getEntity());
        final Collection<AbstractEntity> targets = skill.getEntityTargets();
        final List<AbstractEntity> kept = new ArrayList<>(targets.size());
        for (AbstractEntity target : targets) {
            final Entity entity = BukkitAdapter.adapt(target);
            if (!isForeignPlayer(entity, owner) && !isOutOfBounds(entity, self, owner)) {
                kept.add(target);
            }
        }

        if (kept.size() != targets.size()) {
            skill.setEntityTargets(kept);
        }
    }

    /**
     * Whether the target is a player other than the one who owns the pet.
     *
     * @param owner the pet's owner, null when they are offline, which leaves every player foreign
     */
    private static boolean isForeignPlayer(final Entity entity, final Player owner) {
        return entity instanceof Player player && !player.equals(owner);
    }

    /**
     * Whether the target is something the owner could not hit where it stands. The pet's own skills target the pet
     * itself (its model, its auras), the owner and other pets, and those always stay: a pet standing at spawn is an
     * animal in a {@code damage-animals: deny} region, and dropping it would break every self-targeted mechanic.
     */
    private static boolean isOutOfBounds(final Entity entity, final Entity self, final Player owner) {
        if (entity == null || entity instanceof Player || entity.equals(self) || Pet.getFromEntity(entity) != null) {
            return false;
        }
        return !PetProtection.mayDamage(owner, entity);
    }
}
