package wasmruntime.Operations;

import wasmruntime.ModuleExecutor.Value;
import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

import wasmruntime.Errors.TrapRuntime;

public class Basic {
  public static void nop(ValueStack stack) {

  }

  public static void unreachable(ValueStack stack) {
    throw new TrapRuntime("Unreachable instruction reached");
  }

  public static void drop(ValueStack stack) {
    stack.pop();
  }

  public static void select(ValueStack stack) {
    boolean c = i32(stack) == 0;
    Value b = stack.pop();
    Value a = stack.pop();
    stack.push(c ? b : a);
  }
}
