package fr.nocsy.mcpets.utils;

import fr.nocsy.mcpets.MCPets;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PathFindingUtils {

    private static final Map<UUID, AbstractLocation> registry = new ConcurrentHashMap<>();

    /**
     * Move the entity to the specified location
     */
    public static void moveTo(AbstractEntity entity, AbstractLocation destination) {
        MCPets.getMythicMobs().getVolatileCodeHandler().getAIHandler().navigateToLocation(entity, destination, 1);
    }

    /**
     * Forget the owner's last stop position, once they leave
     */
    public static void remove(UUID owner) {
        registry.remove(owner);
    }

    /**
     * Stop the entity at its location
     */
    public static void stop(AbstractEntity entity, UUID owner) {
        if (registry.get(owner) != null) {
            AbstractLocation loc = registry.get(owner);
            if (loc.getBlockX() == entity.getLocation().getBlockX() &&
                    loc.getBlockY() == entity.getLocation().getBlockY() &&
                    loc.getBlockZ() == entity.getLocation().getBlockZ()) {
                return;
            }
        }

        moveTo(entity, entity.getLocation());
        registry.put(owner, entity.getLocation());
    }
}
