package wasmruntime.Operations.Math.FloatingPoint;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class MinMax {
  public static void Min_F32(ValueStack stack) {
    stack.push(Math.min(f32(stack), f32(stack)));
  }

  public static void Min_F64(ValueStack stack) {
    stack.push(Math.min(f64(stack), f64(stack)));
  }

  public static void Max_F32(ValueStack stack) {
    stack.push(Math.max(f32(stack), f32(stack)));
  }

  public static void Max_F64(ValueStack stack) {
    stack.push(Math.max(f64(stack), f64(stack)));
  }
}
