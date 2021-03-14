package wasmruntime.Operations.Compare;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class le {
  // Implemented as (b >= a) instead of (a <= b) bc it's easier, since you have to pop b from the stack first
  public static void I32_s(ValueStack stack) {
    stack.push(i32(stack) >= i32(stack) ? 1 : 0);
  }

  public static void I32_u(ValueStack stack) {
    stack.push(Integer.compareUnsigned(i32(stack), i32(stack)) >= 0 ? 1 : 0);
  }

  public static void I64_s(ValueStack stack) {
    stack.push(i64(stack) >= i64(stack) ? 1 : 0);
  }

  public static void I64_u(ValueStack stack) {
    stack.push(Long.compareUnsigned(i64(stack), i64(stack)) >= 0 ? 1 : 0);
  }

  public static void F32(ValueStack stack) {
    stack.push(f32(stack) >= f32(stack) ? 1 : 0);
  }

  public static void F64(ValueStack stack) {
    stack.push(f64(stack) >= f64(stack) ? 1 : 0);
  }
}
