package fr.nocsy.mcpets.data;

import fr.nocsy.mcpets.MCPets;
import fr.nocsy.mcpets.utils.debug.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers the pets that were put away because their owner vanished, and brings them back once the
 * owner is visible again.
 *
 * <p>The pet's own AI track is what despawns it, and that track stops itself on the way out, so
 * nothing is left watching that player. This ticker is that watcher: it only ever holds the owners
 * who actually lost a pet to vanish, so it costs nothing while nobody is vanished.</p>
 */
public class VanishedPets {

    /** One second is far below what anyone notices and keeps the idle cost at a single map check. */
    private static final long PERIOD_TICKS = 20L;

    private static final Map<UUID, Set<String>> STASHED = new ConcurrentHashMap<>();

    private VanishedPets() {
    }

    /** Starts the watcher. Called once while the plugin enables. */
    public static void start(final MCPets plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, VanishedPets::tick, PERIOD_TICKS, PERIOD_TICKS);
    }

    /** Records a pet taken away by vanish, so it can be summoned again afterwards. */
    public static void stash(final UUID owner, final String petId) {
        STASHED.computeIfAbsent(owner, key -> new LinkedHashSet<>()).add(petId);
    }

    /**
     * Forgets an owner's stash without restoring it, for the cases where bringing the pet back would
     * be wrong: the owner logged out, or put the pet away themselves while vanished.
     */
    public static void forget(final UUID owner) {
        STASHED.remove(owner);
    }

    private static void tick() {
        if (STASHED.isEmpty()) return;

        for (final UUID owner : new HashSet<>(STASHED.keySet())) {
            final Player player = Bukkit.getPlayer(owner);
            if (player == null) {
                // Their pets come back the way any pet does after a reconnect, not from here.
                STASHED.remove(owner);
                continue;
            }

            if (Pet.isVanished(player)) continue;

            restore(player, STASHED.remove(owner));
        }
    }

    private static void restore(final Player player, final Set<String> petIds) {
        if (petIds == null) return;

        for (final String petId : new ArrayList<>(petIds)) {
            final Pet pet = Pet.getFromId(petId);
            if (pet == null) continue;

            Debugger.send("§6[VanishedPets] : §aBringing §6" + petId + "§a back now that the owner is visible again.");
            pet.spawn(player, player.getLocation());
        }
    }

}
