package fr.nocsy.mcpets.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import fr.nocsy.mcpets.MCPets;
import fr.nocsy.mcpets.data.Pet;

/**
 * Runs work on the thread that owns what it touches. On Paper everything is the main thread, so the inline
 * branches always run and nothing changes there; on Folia/Canvas a hop happens only when the caller is on
 * another region.
 */
public final class Tasks {

    private Tasks() {
    }

    /**
     * Runs {@code task} inline when the current thread owns {@code entity}, otherwise on the entity's next tick.
     * {@code retired} runs instead when the entity is gone before the task could run.
     */
    public static void onEntity(final Entity entity, final Runnable task, final Runnable retired) {
        if (Bukkit.isOwnedByCurrentRegion(entity)) {
            task.run();
            return;
        }
        if (entity.getScheduler().run(MCPets.getInstance(), t -> task.run(), retired) == null && retired != null) {
            retired.run();
        }
    }

    /** Runs {@code task} inline when the current thread owns {@code location}, otherwise on its region. */
    public static void atLocation(final Location location, final Runnable task) {
        if (Bukkit.isOwnedByCurrentRegion(location)) {
            task.run();
            return;
        }
        Bukkit.getRegionScheduler().run(MCPets.getInstance(), location, t -> task.run());
    }

    /**
     * Runs {@code task} on the next tick of the pet's current entity, or on the global region when the pet is not
     * out. The task always runs once, even if the entity goes away in between.
     */
    public static void forPet(final Pet pet, final Runnable task) {
        forPet(pet, task, null);
    }

    private static void forPet(final Pet pet, final Runnable task, final Entity retiredEntity) {
        final Entity entity = pet.getEntityIfStillHere();
        // The entity that just retired can still look present for the rest of this tick.
        if (entity == null || entity == retiredEntity
                || entity.getScheduler().run(MCPets.getInstance(), t -> task.run(), () -> forPet(pet, task, entity)) == null) {
            Bukkit.getGlobalRegionScheduler().run(MCPets.getInstance(), t -> task.run());
        }
    }
}
