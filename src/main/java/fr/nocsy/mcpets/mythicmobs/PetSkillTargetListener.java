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
 * Drops every player but the owner from the targets of a skill a pet casts.
 *
 * <p>Cancelling the damage a pet deals to a player is not enough: the pets attack through skills that also throw,
 * pull, stun, set on fire and hand out wither, and every one of those runs on the skill's target list whatever the
 * damage event decides afterwards. The pets' own skill files carry the filter that keeps a player off that list, and
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
        final Collection<AbstractEntity> targets = skill.getEntityTargets();
        final List<AbstractEntity> kept = new ArrayList<>(targets.size());
        for (AbstractEntity target : targets) {
            if (!isForeignPlayer(target, owner)) {
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
    private static boolean isForeignPlayer(final AbstractEntity target, final Player owner) {
        final Entity entity = BukkitAdapter.adapt(target);
        return entity instanceof Player player && !player.equals(owner);
    }
}
