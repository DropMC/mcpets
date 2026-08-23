package fr.nocsy.mcpets.commands.argument;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;

import fr.nocsy.mcpets.data.Category;
import fr.nocsy.mcpets.data.config.Language;

import org.bukkit.command.CommandSender;

/** Resolves a pet category by its id, suggesting every configured category. */
public class CategoryArgument extends ArgumentResolver<CommandSender, Category> {

    @Override
    protected ParseResult<Category> parse(final Invocation<CommandSender> invocation,
                                          final Argument<Category> context,
                                          final String argument) {
        final Category category = Category.getFromId(argument);
        return category != null
                ? ParseResult.success(category)
                : ParseResult.failure(Language.CATEGORY_DOESNT_EXIST.getComponent());
    }

    @Override
    public SuggestionResult suggest(final Invocation<CommandSender> invocation,
                                    final Argument<Category> argument,
                                    final SuggestionContext context) {
        return SuggestionResult.of(Category.getCategories().stream().map(Category::getId).toList());
    }
}
