package wasmruntime.Operations.Compare;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class eqz {
  public static void I32(ValueStack stack) {
    stack.push(i32(stack) == 0 ? 1 : 0);
  }

  public static void I64(ValueStack stack) {
    stack.push(i64(stack) == 0 ? 1 : 0);
  }
}
