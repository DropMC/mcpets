package fr.nocsy.mcpets.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.auras.Aura;
import io.lumine.mythic.core.skills.auras.AuraRegistry;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import gg.dropmc.survival.core.feature.actionbar.api.ActionBarAPI;
import gg.dropmc.survival.core.feature.actionbar.api.ActionBarHandle;
import gg.dropmc.survival.core.feature.actionbar.api.ActionBarPriority;

import fr.nocsy.mcpets.MCPets;
import fr.nocsy.mcpets.data.config.GlobalConfig;
import fr.nocsy.mcpets.data.config.Language;

/**
 * The countdown until a pet can attack again, shown on the action bar.
 *
 * <p>The wait itself belongs to MythicMobs: the attack skill hangs an aura on the owner for exactly as
 * long as the skill's own cooldown, and the pack's conditions read that aura to decide whether an order
 * goes through. Rather than keep a second clock that would drift from it, this reads the remaining ticks
 * off that aura, so what the player sees is the same number the skill is counting down.</p>
 */
public final class PetAttackCooldown {

    private static final Map<UUID, ActionBarHandle> displays = new HashMap<>();

    private PetAttackCooldown() {}

    /** Whole seconds left before the pet can attack again, or 0 when it is ready. */
    public static int remainingSeconds(final Player player) {
        return (int) Math.ceil(remainingTicks(player) / 20.0);
    }

    /**
     * Starts the action bar countdown, unless one is already running for this player. It takes itself
     * down when the aura runs out, announcing that the pet is ready again.
     *
     * @param player the pet's owner, who just gave an attack order
     */
    public static void watch(final Player player) {
        final UUID uuid = player.getUniqueId();
        if (displays.containsKey(uuid) || remainingTicks(player) <= 0) return;

        displays.put(uuid, ActionBarAPI.source(player, ActionBarPriority.NORMAL, () -> line(player)));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline() && remainingTicks(player) > 0) return;

                cancel();
                clear(uuid);
                if (player.isOnline()) Language.ATTACK_READY.sendMessage(player);
            }
        }.runTaskTimer(MCPets.getInstance(), 10L, 10L);
    }

    /** Drops the countdown for a player, e.g. when their pet is stored or they disconnect. */
    public static void clear(final UUID uuid) {
        final ActionBarHandle handle = displays.remove(uuid);
        if (handle != null) handle.remove();
    }

    private static Component line(final Player player) {
        return Component.text("Ataque em ", NamedTextColor.GRAY)
                .append(Component.text(remainingSeconds(player) + "s", NamedTextColor.WHITE));
    }

    /**
     * Ticks left on the cooldown aura the attack skill hangs on the owner. Several trackers can pile up
     * under the same name if a pet is swapped mid-cooldown, so the longest one wins: it is the one that
     * decides when the next order actually goes through.
     */
    private static int remainingTicks(final Player player) {
        final AuraRegistry registry = MythicBukkit.inst().getSkillManager().getAuraManager()
                .getAuraRegistry(BukkitAdapter.adapt(player));
        if (registry == null) return 0;

        final Queue<Aura.AuraTracker> trackers = registry.getAuras().get(GlobalConfig.getInstance().getAttackCooldownAura());
        if (trackers == null) return 0;

        return trackers.stream().mapToInt(Aura.AuraTracker::getTicksRemaining).max().orElse(0);
    }
}
