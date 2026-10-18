package fr.nocsy.mcpets.data.flags;

import java.util.UUID;

import org.bukkit.entity.Player;

public interface StoppableFlag {

    /**
     * Prepare the flag's periodic check
     * Returns false when the flag can not be checked
     */
    boolean launch();

    /**
     * One pass of the flag's check for one owner, run on that owner's thread
     */
    void check(UUID owner, Player player);
}
