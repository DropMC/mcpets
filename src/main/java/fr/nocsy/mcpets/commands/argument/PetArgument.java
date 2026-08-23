package fr.nocsy.mcpets.commands.argument;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;

import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.data.config.Language;

import org.bukkit.command.CommandSender;

/** Resolves a registered pet by its id, suggesting every id the server knows. */
public class PetArgument extends ArgumentResolver<CommandSender, Pet> {

    @Override
    protected ParseResult<Pet> parse(final Invocation<CommandSender> invocation,
                                     final Argument<Pet> context,
                                     final String argument) {
        final Pet pet = Pet.getFromId(argument);
        return pet != null
                ? ParseResult.success(pet)
                : ParseResult.failure(Language.PET_DOESNT_EXIST.getComponent());
    }

    @Override
    public SuggestionResult suggest(final Invocation<CommandSender> invocation,
                                    final Argument<Pet> argument,
                                    final SuggestionContext context) {
        return SuggestionResult.of(Pet.getObjectPets().stream().map(Pet::getId).toList());
    }
}
