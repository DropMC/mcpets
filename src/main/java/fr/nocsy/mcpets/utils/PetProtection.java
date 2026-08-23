package fr.nocsy.mcpets.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.nocsy.mcpets.data.config.GlobalConfig;

/**
 * Whether a pet may fight, answered by what its owner is allowed to do.
 *
 * <p>The pets attack through MythicMobs' native {@code damage} mechanic, which no protection plugin
 * reads as a player action. Left alone, a pet kills at spawn what its owner cannot even scratch, and
 * knocks around villagers and NPCs its owner is not allowed to touch. Both checks here hand the
 * question to WorldGuard so the pet is bound by exactly the flags its owner is.</p>
 */
public final class PetProtection {

    private PetProtection() {}

    /**
     * Whether {@code owner} would be allowed to hit {@code victim} where it stands. WorldGuard runs the
     * same query it runs for a real hit, silently, so no deny message is spammed while a pet keeps
     * swinging at something it cannot hurt.
     *
     * @param owner  the pet's owner, null when they are offline
     * @param victim the entity about to take the damage
     */
    public static boolean mayDamage(final Player owner, final Entity victim) {
        if (owner == null || victim == null) return true;
        if (!GlobalConfig.getInstance().isWorldGuardSupport()) return true;

        return WorldGuardPlugin.inst().createProtectionQuery().testEntityDamage(owner, victim);
    }

    /**
     * Whether {@code player} may order an attack from where they stand. This is the gate on the stick,
     * checked before the skill runs at all: a cancelled damage event still leaves the cast's knockback,
     * particles and stuns behind, and at spawn none of that should happen either.
     *
     * <p>The question is asked as {@code damage-animals}, which is the flag that already stops the player
     * from hitting anything at spawn. Only an explicit {@code deny} refuses: the flag is unset in the
     * survival world, and an unset flag reads back as no value at all, which {@code testState} would
     * report as "not allowed" and turn every world into a sanctuary.</p>
     *
     * @param player the pet's owner, giving the order
     */
    public static boolean mayFight(final Player player) {
        if (!GlobalConfig.getInstance().isWorldGuardSupport()) return true;

        final RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        final LocalPlayer local = WorldGuardPlugin.inst().wrapPlayer(player);
        final State state = query.queryState(BukkitAdapter.adapt(player.getLocation()), local, Flags.DAMAGE_ANIMALS);

        return state != State.DENY;
    }
}
