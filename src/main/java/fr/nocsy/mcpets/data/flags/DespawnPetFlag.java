package fr.nocsy.mcpets.data.flags;

import java.util.UUID;
import java.util.ArrayList;

import org.bukkit.entity.Player;

import fr.nocsy.mcpets.MCPets;
import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.data.config.Language;
import fr.nocsy.mcpets.data.PetDespawnReason;

public class DespawnPetFlag extends AbstractFlag implements StoppableFlag {

    public static String NAME = "mcpets-despawn";

    public DespawnPetFlag(final MCPets instance) {
        super(NAME, false, instance);
    }

    @Override
    public void register() {
        super.register();
    }

    @Override
    public boolean launch() {
        if (getFlag() == null) {
            MCPets.getLog().warning("Flag " + getFlagName() + " couldn't not be launched as it's null. Please contact Nocsy.");
            return false;
        }

        MCPets.getLog().info("Starting flag " + getFlagName() + ".");
        return true;
    }

    @Override
    public void check(final UUID owner, final Player pl) {
        if (pl == null) return;

        if (!testState(pl.getLocation())) return;

        for (Pet pet : new ArrayList<>(Pet.getActivePetsForOwner(owner))) {
            pet.despawn(PetDespawnReason.TELEPORT);
        }

        Language.CANT_FOLLOW_HERE.sendMessage(pl);
    }

}
