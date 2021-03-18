package wasmruntime.Operations.Conversions;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class reinterpret {
  public static void f32_i32(ValueStack stack) {
    stack.push(Float.floatToIntBits(f32(stack)));
  }

  public static void i32_f32(ValueStack stack) {
    stack.push(Float.intBitsToFloat(i32(stack)));
  }

  public static void f64_i64(ValueStack stack) {
    stack.push(Double.doubleToLongBits(f64(stack)));
  }

  public static void i64_f64(ValueStack stack) {
    stack.push(Double.longBitsToDouble(i64(stack)));
  }
}
