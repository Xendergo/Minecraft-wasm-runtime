package wasmruntime.Commands.Suggestions;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import static com.mojang.brigadier.arguments.StringArgumentType.*;

import net.minecraft.server.command.ServerCommandSource;
import wasmruntime.Modules;

public class ExportedFunctions implements SuggestionProvider<ServerCommandSource> {
	@Override
	public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
    // Modules.modules.get(getString(context, "Module")).exportedFunctions.keySet().forEach(name -> builder.suggest(name));

		return builder.buildFuture();
	}
}
