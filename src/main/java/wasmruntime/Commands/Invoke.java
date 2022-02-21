package wasmruntime.Commands;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import wasmruntime.ModuleWrapper;
import wasmruntime.Modules;
import wasmruntime.Exceptions.WasmtimeEmbeddingException;
import wasmruntime.Types.FuncSignature;
import wasmruntime.Types.Value;
import wasmruntime.Utils.Message;

public class Invoke {
    private Invoke() {
    }

    public static int run(CommandContext<ServerCommandSource> ctx, String moduleName, String functionName,
            String arguments) throws CommandSyntaxException {
        if (!Modules.moduleExists(moduleName))
            throw new SimpleCommandExceptionType(new TranslatableText("wasm.commands.error.no_such_module", moduleName))
                    .create();
        ModuleWrapper module = Modules.getModule(moduleName);

        if (!module.exportedFunctions.containsKey(functionName))
            throw new SimpleCommandExceptionType(
                    new TranslatableText("wasm.commands.error.no_such_function", functionName)).create();
        FuncSignature func = module.exportedFunctions.get(functionName);

        String[] argsStrings = StringUtils.split(arguments, " ");
        if (argsStrings.length != func.inputs.length)
            throw new SimpleCommandExceptionType(
                    new TranslatableText("wasm.commands.error.incorrect_arg_amt", func.inputs.length)).create();

        List<Value<?>> stack = new ArrayList<>();

        for (int i = 0; i < argsStrings.length; i++) {
            try {
                stack.add(Value.parse(argsStrings[i], func.inputs[i]));
            } catch (Exception e) {
                throw new SimpleCommandExceptionType(
                        new TranslatableText("wasm.commands.error.incorrect_argument", argsStrings[i], func.inputs[i]))
                                .create();
            }
        }

        List<Value<?>> output;

        try {
            output = module.CallExport(functionName, stack);
        } catch (WasmtimeEmbeddingException e) {
            throw new SimpleCommandExceptionType(new LiteralText(e.getMessage())).create();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

        Message.broadcast(ctx.getSource().getServer(), new TranslatableText("wasm.commands.invoke.return", output));

        return Command.SINGLE_SUCCESS;
    }
}
