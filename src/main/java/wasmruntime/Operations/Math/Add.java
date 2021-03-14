package wasmruntime.Operations.Math;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class Add {
  public static void I32(ValueStack stack) {
    stack.push(i32(stack) + i32(stack));
  }

  public static void I64(ValueStack stack) {
    stack.push(i64(stack) + i64(stack));
  }

  public static void F32(ValueStack stack) {
    stack.push(f32(stack) + f32(stack));
  }

  public static void F64(ValueStack stack) {
    stack.push(f64(stack) + f64(stack));
  }
}
