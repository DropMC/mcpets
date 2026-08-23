package fr.nocsy.mcpets.commands.argument;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;

import fr.nocsy.mcpets.data.config.Language;
import fr.nocsy.mcpets.data.config.PetFoodConfig;
import fr.nocsy.mcpets.data.livingpets.PetFood;

import org.bukkit.command.CommandSender;

/** Resolves a pet food by its id, suggesting every configured food. */
public class PetFoodArgument extends ArgumentResolver<CommandSender, PetFood> {

    @Override
    protected ParseResult<PetFood> parse(final Invocation<CommandSender> invocation,
                                         final Argument<PetFood> context,
                                         final String argument) {
        final PetFood food = PetFood.getFromId(argument);
        return food != null
                ? ParseResult.success(food)
                : ParseResult.failure(Language.PETFOOD_DOESNT_EXIST.getComponent());
    }

    @Override
    public SuggestionResult suggest(final Invocation<CommandSender> invocation,
                                    final Argument<PetFood> argument,
                                    final SuggestionContext context) {
        return SuggestionResult.of(PetFoodConfig.getInstance().list().stream().map(PetFood::getId).toList());
    }
}
