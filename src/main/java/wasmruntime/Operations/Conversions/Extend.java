package wasmruntime.Operations.Conversions;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class Extend {
  public static void I32_s(ValueStack stack) {
    stack.push((long) i32(stack));
  }

  public static void I32_u(ValueStack stack) {
    stack.push(((long) i32(stack)) & 0xFFFFFFFFL);
  }

  public static void I32_8(ValueStack stack) {
    stack.push(i32(stack) << 24 >> 24);
  }

  public static void I32_16(ValueStack stack) {
    stack.push(i32(stack) << 16 >> 16);
  }

  public static void I64_8(ValueStack stack) {
    stack.push(i64(stack) << 56 >> 56);
  }

  public static void I64_16(ValueStack stack) {
    stack.push(i64(stack) << 48 >> 48);
  }

  public static void I64_32(ValueStack stack) {
    stack.push(i64(stack) << 32 >> 32);
  }
}
