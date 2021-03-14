package wasmruntime.Operations.Math;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class Subtract {
  public static void I32(ValueStack stack) {
    int b = i32(stack);
    stack.push(i32(stack) - b);
  }

  public static void I64(ValueStack stack) {
    long b = i64(stack);
    stack.push(i64(stack) - b);
  }

  public static void F32(ValueStack stack) {
    float b = f32(stack);
    stack.push(f32(stack) - b);
  }

  public static void F64(ValueStack stack) {
    double b = f64(stack);
    stack.push(f64(stack) - b);
  }
}
