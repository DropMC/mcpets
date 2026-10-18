package fr.nocsy.mcpets.data.flags;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.nocsy.mcpets.MCPets;
import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.utils.Tasks;

public class FlagsManager {

    private static final List<AbstractFlag> flags = new CopyOnWriteArrayList<>();

    @Getter
    private static boolean registered;

    private static ScheduledTask task;

    public static void init(MCPets instance) {
        flags.clear();
        registered = true;

        if (instance == null) {
            MCPets.getLog().warning("The main instance is null. The flags could not be registered...");
            return;
        }

        flags.add(new DismountPetFlag(instance));
        flags.add(new DespawnPetFlag(instance));
        flags.add(new DismountFlyPetFlag(instance));

        for (AbstractFlag flag : flags) {
            flag.register();
        }
    }

    /**
     * Get the abstract flag registered
     */
    public static AbstractFlag getFlag(String flagName) {
        return flags.stream().filter(f -> f.getFlagName().equals(flagName)).findFirst().orElse(null);
    }

    /**
     * Start the schedulers
     */
    public static void launchFlags() {
        MCPets.getLog().info("-=- Launching Flags -=-");
        int count = 0;
        final List<StoppableFlag> launched = new ArrayList<>();
        for (AbstractFlag flag : flags) {
            if (!(flag instanceof StoppableFlag stoppableFlag)) continue;
            if (stoppableFlag.launch()) launched.add(stoppableFlag);
            count++;
        }
        MCPets.getLog().info(count + " flags launched.");

        if (launched.isEmpty()) return;
        // One timer for every flag. Each owner is checked on their own thread, in the flags' order.
        task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(MCPets.getInstance(), t -> {
            if (MCPets.getMythicMobs() == null) return;

            for (final UUID owner : Pet.getActivePets().keySet()) {
                final Player p = Bukkit.getPlayer(owner);
                if (p == null) continue;

                Tasks.onEntity(p, () -> {
                    for (final StoppableFlag flag : launched) {
                        flag.check(owner, p);
                    }
                }, null);
            }
        }, 1L, 20L);
    }

    /**
     * Stop the schedulers
     */
    public static void stopFlags() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        flags.clear();
    }

}
