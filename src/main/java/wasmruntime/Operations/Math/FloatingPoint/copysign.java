package wasmruntime.Operations.Math.FloatingPoint;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class copysign {
  public static void F32(ValueStack stack) {
    stack.push(Float.intBitsToFloat((Float.floatToRawIntBits(f32(stack)) & 0x80000000) | (Float.floatToRawIntBits(f32(stack)) & 0x7FFFFFFF)));
  }

  public static void F64(ValueStack stack) {
    stack.push(Double.longBitsToDouble((Double.doubleToRawLongBits(f64(stack)) & 0x8000000000000000L) | (Double.doubleToRawLongBits(f64(stack)) & 0x7FFFFFFFFFFFFFFFL)));
  }
}
