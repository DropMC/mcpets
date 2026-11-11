package fr.nocsy.mcpets.listeners;

import fr.nocsy.mcpets.data.Pet;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.util.BoundingBox;

/**
 * Lets a block go down where only a pet stands. A pet follows right at its owner's feet, so its
 * (invisible) base entity sat in the block space of every pillar-up or bridge placement and vanilla
 * refused the block. Any other entity in the way still blocks as before.
 */
public class PetBuildObstructionListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onCanBuild(final BlockCanBuildEvent event) {
        if (event.isBuildable() || event.getPlayer() == null)
            return;

        final Block block = event.getBlock();
        // The refusal may come from the block itself (a torch on air), not from an entity
        if (!block.canPlace(event.getBlockData()))
            return;

        boolean petInTheWay = false;
        for (final Entity entity : block.getWorld().getNearbyEntities(BoundingBox.of(block))) {
            if (Pet.getFromEntity(entity) != null) {
                petInTheWay = true;
            } else if (blocksBuilding(entity)) {
                return;
            }
        }

        if (petInTheWay)
            event.setBuildable(true);
    }

    private static boolean blocksBuilding(final Entity entity) {
        if (entity instanceof Item || entity instanceof ExperienceOrb || entity instanceof Projectile
                || entity instanceof AreaEffectCloud || entity instanceof Display || entity instanceof Interaction)
            return false;
        return !(entity instanceof final Player player) || player.getGameMode() != GameMode.SPECTATOR;
    }
}
