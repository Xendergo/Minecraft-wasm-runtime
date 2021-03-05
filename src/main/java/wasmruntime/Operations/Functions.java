package wasmruntime.Operations;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

import wasmruntime.Errors.Trap;
import wasmruntime.Errors.TrapRuntime;
import wasmruntime.ModuleData.WasmFunctionInterface;

public class Functions {
  public static void call(ValueStack stack) {
    try {
      stack.module.Functions.get(i32(immediates[0])).Exec(stack);
    } catch (Trap trap) {
      throw new TrapRuntime(trap.getMessage());
    }
  }

  public static void callIndirect(ValueStack stack) {
    WasmFunctionInterface func = stack.module.Functions.get(stack.module.Tables.get(0).values.get(i32(stack.pop())));
    if (!func.type.equals(stack.module.TypeSection.get(i32(immediates[0])))) throw new TrapRuntime("call_indirect tried to call a function with the wrong type, are you passing an incorrect callback?");
    try {
      func.Exec(stack);
    } catch (Trap trap) {
      throw new TrapRuntime(trap.getMessage());
    }
  }
}
