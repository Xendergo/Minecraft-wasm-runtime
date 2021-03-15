package wasmruntime.Operations.Math.FloatingPoint;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class abs {
  public static void F32(ValueStack stack) {
    stack.push(Math.abs(f32(stack)));
  }

  public static void F64(ValueStack stack) {
    stack.push(Math.abs(f64(stack)));
  }
}
