package wasmruntime.Operations.Bitwise;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class clz {
  public static void I32(ValueStack stack) {
    stack.push(Integer.numberOfLeadingZeros(i32(stack)));
  }

  public static void I64(ValueStack stack) {
    stack.push(Long.numberOfLeadingZeros(i64(stack)));
  }
}
