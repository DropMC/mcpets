package fr.nocsy.mcpets.commands;

import dev.rollczi.litecommands.LiteCommands;

import fr.nocsy.mcpets.MCPets;
import fr.nocsy.mcpets.commands.argument.CategoryArgument;
import fr.nocsy.mcpets.commands.argument.PetArgument;
import fr.nocsy.mcpets.commands.argument.PetFoodArgument;
import fr.nocsy.mcpets.data.Category;
import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.data.livingpets.PetFood;

import gg.dropmc.survival.core.api.bukkit.plugin.CommandFactory;
import gg.dropmc.survival.core.feature.help.api.HelpAPI;

import org.bukkit.command.CommandSender;

/** Registers the commands through core, which is also what puts them in the generated help. */
public final class CommandRegistry {

    private static LiteCommands<CommandSender> commands;

    private CommandRegistry() {}

    public static void register(final MCPets plugin) {
        commands = CommandFactory.defaultBuilder(plugin)
                .commands(new PetCommand(), new MountCommand())
                .argument(Pet.class, new PetArgument())
                .argument(Category.class, new CategoryArgument())
                .argument(PetFood.class, new PetFoodArgument())
                .build();

        HelpAPI.register(plugin, commands);
    }

    public static void unregister(final MCPets plugin) {
        if (commands == null) {
            return;
        }

        HelpAPI.unregister(plugin);
        commands.unregister();
        commands = null;
    }
}
