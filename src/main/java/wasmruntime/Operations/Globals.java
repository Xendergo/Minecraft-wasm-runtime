package wasmruntime.Operations;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class Globals {
  public static void set(ValueStack stack) {
    stack.module.Globals.get(i32(immediates[0])).setValue(stack.pop());
  }

  public static void get(ValueStack stack) {
    stack.push(stack.module.Globals.get(i32(immediates[0])).getValue());
  }
}
