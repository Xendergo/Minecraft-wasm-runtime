package wasmruntime.Commands;

import java.io.File;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import wasmruntime.Modules;
import wasmruntime.WasmRuntime;

public class Load {
  public static int run(CommandContext<ServerCommandSource> ctx, String moduleName) throws CommandSyntaxException {
    try {
      Modules.LoadModule(new File(WasmRuntime.configFolder, moduleName), moduleName);
    } catch (Exception e) {
      throw new SimpleCommandExceptionType(new LiteralText(e.getMessage())).create();
    }

    return Command.SINGLE_SUCCESS;
  }
}
