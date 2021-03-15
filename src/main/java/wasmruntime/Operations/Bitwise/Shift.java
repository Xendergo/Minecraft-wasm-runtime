package wasmruntime.Operations.Bitwise;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class Shift {
  public static void shl_i32(ValueStack stack) {
    int b = i32(stack);
    stack.push(i32(stack) << b);
  }

  public static void shr_i32_s(ValueStack stack) {
    int b = i32(stack);
    stack.push(i32(stack) >> b);
  }

  public static void shr_i32_u(ValueStack stack) {
    int b = i32(stack);
    stack.push(i32(stack) >>> b);
  }

  public static void shl_i64(ValueStack stack) {
    long b = i64(stack);
    stack.push(i64(stack) << b);
  }

  public static void shr_i64_s(ValueStack stack) {
    long b = i64(stack);
    stack.push(i64(stack) >> b);
  }

  public static void shr_i64_u(ValueStack stack) {
    long b = i64(stack);
    stack.push(i64(stack) >>> b);
  }
}
