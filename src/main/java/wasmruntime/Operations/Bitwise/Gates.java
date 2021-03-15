package wasmruntime.Operations.Bitwise;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class Gates {
  public static void and_i32(ValueStack stack) {
    stack.push(i32(stack) & i32(stack));
  }

  public static void or_i32(ValueStack stack) {
    stack.push(i32(stack) | i32(stack));
  }

  public static void xor_i32(ValueStack stack) {
    stack.push(i32(stack) ^ i32(stack));
  }

  public static void and_i64(ValueStack stack) {
    stack.push(i64(stack) & i64(stack));
  }

  public static void or_i64(ValueStack stack) {
    stack.push(i64(stack) | i64(stack));
  }

  public static void xor_i64(ValueStack stack) {
    stack.push(i64(stack) ^ i64(stack));
  }
}
