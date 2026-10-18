package fr.nocsy.mcpets.data.flags;

import fr.nocsy.mcpets.MCPets;
import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.data.config.Language;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DismountFlyPetFlag extends AbstractFlag implements StoppableFlag {

    public static String NAME = "mcpets-dismount-flying";

    public DismountFlyPetFlag(final MCPets instance) {
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
        else {
            MCPets.getLog().info("Starting flag " + getFlagName() + ".");
        }
        return true;
    }

    @Override
    public void check(final UUID owner, final Player p) {
        for (final Pet pet : Pet.getActivePetsForOwner(owner)) {

            if (!pet.isMountable())
                continue;

            if (!MCPets.getModeler().isFlyingMount(pet, owner))
                continue;

            if (p != null) {
                if (!pet.hasMount(p))
                    continue;

                final boolean hasToBeEjected = testState(p.getLocation());

                if (hasToBeEjected) {
                    pet.dismount(p);
                    Language.NOT_MOUNTABLE_HERE.sendMessage(p);
                }
            }
        }
    }
}
