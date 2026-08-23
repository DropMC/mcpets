package fr.nocsy.mcpets.commands;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.optional.OptionalArg;
import dev.rollczi.litecommands.annotations.permission.Permission;

import fr.nocsy.mcpets.PPermission;
import fr.nocsy.mcpets.data.CategoryType;
import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.data.inventories.CategoriesMenu;
import fr.nocsy.mcpets.data.inventories.MountInteractionMenu;

import org.bukkit.entity.Player;

@Command(name = "montaria", aliases = {"montarias", "mounts"})
@Permission(PPermission.MOUNT_NODE)
public class MountCommand {

    @Execute
    @Description("Abre o menu das suas montarias.")
    public void menu(final @Context Player player) {
        CategoriesMenu.openFiltered(player, CategoryType.MOUNT);
    }

    @Execute(name = "menu")
    @Description("Abre as ações da sua montaria ativa.")
    public void actions(final @Context Player player, final @OptionalArg("montaria") Pet mount) {
        final Pet active = PetCommand.resolveActive(player, mount, true);
        if (active == null) {
            return;
        }

        new MountInteractionMenu(active, player.getUniqueId()).open(player);
    }
}
