package wasmruntime.Operations.Bitwise;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class ctz {
  public static void I32(ValueStack stack) {
    stack.push(Integer.numberOfTrailingZeros(i32(stack)));
  }

  public static void I64(ValueStack stack) {
    stack.push(Long.numberOfTrailingZeros(i64(stack)));
  }
}
