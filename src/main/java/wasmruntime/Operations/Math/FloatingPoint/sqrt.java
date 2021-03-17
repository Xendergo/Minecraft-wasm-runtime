package wasmruntime.Operations.Math.FloatingPoint;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class sqrt {
  public static void F32(ValueStack stack) {
    stack.push(Math.sqrt(f32(stack)));
  }

  public static void F64(ValueStack stack) {
    stack.push(Math.sqrt(f64(stack)));
  }
}
