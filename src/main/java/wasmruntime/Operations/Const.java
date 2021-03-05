package wasmruntime.Operations;

import wasmruntime.ModuleData.Opcodes;
import wasmruntime.ModuleExecutor.ValueStack;

public class Const {
  public static void ConstOperation(ValueStack stack) {
    stack.push(Opcodes.immediates[0]);
  }
}
