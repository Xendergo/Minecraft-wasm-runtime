package wasmruntime.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import wasmruntime.Modules;
import wasmruntime.Errors.Trap;
import wasmruntime.ModuleData.Module;
import wasmruntime.ModuleData.WasmFunctionInterface;
import wasmruntime.ModuleExecutor.ValueF32;
import wasmruntime.ModuleExecutor.ValueF64;
import wasmruntime.ModuleExecutor.ValueI32;
import wasmruntime.ModuleExecutor.ValueI64;
import wasmruntime.ModuleExecutor.ValueStack;

public class Invoke {
  public static int run(CommandContext<ServerCommandSource> ctx, String moduleName, String functionName, String arguments) throws CommandSyntaxException {
    if (!Modules.modules.containsKey(moduleName)) throw new SimpleCommandExceptionType(new TranslatableText("wasm.commands.error.no_such_module", moduleName)).create();
    Module module = Modules.modules.get(moduleName);

    if (!module.exportedFunctions.containsKey(functionName)) throw new SimpleCommandExceptionType(new TranslatableText("wasm.commands.error.no_such_function", functionName)).create();
    WasmFunctionInterface func = module.Functions.get(module.exportedFunctions.get(functionName).index);

    String[] argsStrings = StringUtils.split(arguments, " ");
    if (argsStrings.length != func.type.inputs.length) throw new SimpleCommandExceptionType(new TranslatableText("wasm.commands.error.incorrect_arg_amt", func.type.inputs.length)).create();
    
    ValueStack stack = new ValueStack(func.getStackSize(), module);

    for (int i = 0; i < argsStrings.length; i++) {
      try {
        switch (func.type.inputs[i]) {
          case i32:
          stack.push(new ValueI32(Integer.parseInt(argsStrings[i])));
          break;
  
          case i64:
          stack.push(new ValueI64(Long.parseLong(argsStrings[i])));
          break;
  
          case f32:
          stack.push(new ValueF32(Float.parseFloat(argsStrings[i])));
          break;
  
          case f64:
          stack.push(new ValueF64(Double.parseDouble(argsStrings[i])));
          break;
  
          default:
          throw new Exception("");
        }
      } catch (Exception e) {
        throw new SimpleCommandExceptionType(new TranslatableText("wasm.commands.error.incorrect_argument", argsStrings[i], func.type.inputs[i])).create();
      }
    }

    try {
		  func.Exec(stack);
    } catch (Trap e) {
      throw new SimpleCommandExceptionType(new LiteralText(e.getMessage())).create();
    }

    System.out.println(stack);

    return Command.SINGLE_SUCCESS;
  }
}
