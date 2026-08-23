package fr.nocsy.mcpets.listeners;

import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import fr.nocsy.mcpets.data.Items;
import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.data.PlayerSignal;
import fr.nocsy.mcpets.data.config.FormatArg;
import fr.nocsy.mcpets.data.config.Language;
import fr.nocsy.mcpets.utils.PetProtection;

/**
 * Commanding a pet with a stick. Any stick works, and only while the player has a pet out, so there
 * is nothing to hand out, nothing to lose and nothing to protect from being dropped or crafted.
 */
public class SignalStickListener implements Listener {

    @EventHandler
    public void switchSignal(final PlayerInteractEvent e) {
        if (e.getAction() != Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        final Player p = e.getPlayer();
        if (!holdsStick(p)) return;

        final UUID owner = p.getUniqueId();
        final Pet pet = Pet.fromOwner(owner);
        if (pet == null) return;

        final String nextSignal = PlayerSignal.getNextSignal(owner);
        if (nextSignal == null) return;

        PlayerSignal.setSignal(owner, nextSignal);
        p.sendActionBar(Language.SIGNAL_STICK_SIGNAL.getComponentFormatted(
                new FormatArg("%signal%", nextSignal.toLowerCase().replace("_", " "))));
    }

    @EventHandler
    public void castSkill(final PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // A stick is an ordinary item now, so right-clicking a chest or a door has to keep opening it
        // instead of casting: the pet answers to the clicks that would otherwise do nothing.
        final Block clicked = e.getClickedBlock();
        if (clicked != null && clicked.getType().isInteractable() && !e.getPlayer().isSneaking()) {
            return;
        }

        if (checkSkillCast(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void castSkill(final PlayerInteractAtEntityEvent e) {
        if (checkSkillCast(e.getPlayer())) e.setCancelled(true);
    }

    private boolean checkSkillCast(final Player p) {
        if (!holdsStick(p)) return false;

        final Pet pet = Pet.fromOwner(p.getUniqueId());
        if (pet == null || !pet.isStillHere()) return false;

        // Refused before the skill runs, not after: cancelling the damage it deals would still leave the
        // knockback, the stuns and the particles going off in a place where none of that belongs.
        if (!PetProtection.mayFight(p)) {
            Language.NO_ATTACK_HERE.sendMessage(p);
            return true;
        }

        pet.sendSignal(PlayerSignal.getSignalTag(p.getUniqueId()));
        return true;
    }

    /** Whether either hand holds a stick. */
    private boolean holdsStick(final Player p) {
        final ItemStack main = p.getInventory().getItemInMainHand();
        return Items.isSignalStick(main) || Items.isSignalStick(p.getInventory().getItemInOffHand());
    }
}
