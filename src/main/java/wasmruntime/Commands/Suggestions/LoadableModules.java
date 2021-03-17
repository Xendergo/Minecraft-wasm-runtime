package wasmruntime.Commands.Suggestions;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import org.apache.commons.io.FilenameUtils;

import net.minecraft.server.command.ServerCommandSource;
import wasmruntime.WasmRuntime;

public class LoadableModules implements SuggestionProvider<ServerCommandSource> {

	@Override
	public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
    try {
		  Arrays.stream(WasmRuntime.configFolder.listFiles()).forEach(name -> builder.suggest(FilenameUtils.getBaseName(name.getName())));
    } catch (Exception e) {
      throw new CommandSyntaxException(new SimpleCommandExceptionType(new LiteralMessage(e.getMessage())), new LiteralMessage(e.getMessage()));
    }

		return builder.buildFuture();
	}
}

