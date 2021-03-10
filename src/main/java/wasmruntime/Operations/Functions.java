package wasmruntime.Operations;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

import wasmruntime.Errors.Trap;
import wasmruntime.Errors.TrapRuntime;
import wasmruntime.ModuleData.Table;
import wasmruntime.ModuleData.WasmFunctionInterface;

public class Functions {
  public static void call(ValueStack stack) {
    try {
      WasmFunctionInterface func = stack.module.Functions.get(i32(immediates[0]));
      
      ValueStack newStack = func.Exec(func.popArgs(stack), stack.module);
      stack.pushStack(newStack);
    } catch (Trap trap) {
      throw new TrapRuntime(trap.getMessage());
    }
  }

  public static void callIndirect(ValueStack stack) {
    Table table = stack.module.Tables.get(0);
    if (table.values.size() <= i32(stack.peek())) throw new TrapRuntime("call_indirect tried to access the index " + i32(stack.peek()) + " which isn't in the table");
    int funcIndex = table.values.get(i32(stack));
    if (stack.module.Functions.size() <= funcIndex) throw new TrapRuntime("call_indirect tried to call a fuction with a funcref that doesn't exist");
    WasmFunctionInterface func = stack.module.Functions.get(funcIndex);
    if (!func.type.equals(stack.module.TypeSection.get(i32(immediates[0])))) throw new TrapRuntime("call_indirect tried to call a function with the wrong type, are you passing an incorrect callback?");
    try {
      ValueStack newStack = func.Exec(func.popArgs(stack), stack.module);
      stack.pushStack(newStack);
    } catch (Trap trap) {
      throw new TrapRuntime(trap.getMessage());
    }
  }
}
