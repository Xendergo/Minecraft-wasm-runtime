package wasmruntime.Operations.Conversions;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class wrap {
  public static void I32(ValueStack stack) {
    stack.push((int) (i64(stack) & 0xFFFFFFFF));
  }
}
