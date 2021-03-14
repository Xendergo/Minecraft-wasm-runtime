package wasmruntime.Operations.Bitwise;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class popcnt {
  public static void I32(ValueStack stack) {
    stack.push(Integer.bitCount(i32(stack)));
  }

  public static void I64(ValueStack stack) {
    stack.push(Long.bitCount(i64(stack)));
  }
}
