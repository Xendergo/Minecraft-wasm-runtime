package wasmruntime.Operations.Compare;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class eq {
  public static void I32(ValueStack stack) {
    stack.push(i32(stack) == i32(stack) ? 1 : 0);
  }

  public static void I64(ValueStack stack) {
    stack.push(i64(stack) == i64(stack) ? 1 : 0);
  }

  public static void F32(ValueStack stack) {
    stack.push(i64(stack) == i64(stack) ? 1 : 0);
  }

  public static void F64(ValueStack stack) {
    stack.push(i64(stack) == i64(stack) ? 1 : 0);
  }
}
