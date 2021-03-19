package wasmruntime.Operations.Math.FloatingPoint;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class Rounding {
  public static void ceil_f32(ValueStack stack) {
    stack.push((float)(Math.ceil(f32(stack))));
  }

  public static void floor_f32(ValueStack stack) {
    stack.push((float)(Math.floor(f32(stack))));
  }

  public static void trunc_f32(ValueStack stack) {
    Float v = f32(stack);
    stack.push((float)(Math.signum(v) * Math.floor(Math.abs(v))));
  }

  // idk why it uses such a weird rounding method   https://github.com/sunfishcode/wasm-reference-manual/blob/master/WebAssembly.md#floating-point-round-to-nearest-integer
  public static void nearest_f32(ValueStack stack) {
    float v = f32(stack);
    stack.push(v == 0.5 ? (Math.floor(v) % 2 == 0 ? Math.floor(v) : Math.ceil(v)) : Math.round(v));
  }

  public static void ceil_f64(ValueStack stack) {
    stack.push(Math.ceil(f64(stack)));
  }

  public static void floor_f64(ValueStack stack) {
    stack.push(Math.floor(f64(stack)));
  }

  public static void trunc_f64(ValueStack stack) {
    double v = f64(stack);
    stack.push(Math.signum(v) * Math.floor(Math.abs(v)));
  }

  // idk why it uses such a weird rounding method   https://github.com/sunfishcode/wasm-reference-manual/blob/master/WebAssembly.md#floating-point-round-to-nearest-integer
  public static void nearest_f64(ValueStack stack) {
    double v = f64(stack);
    stack.push(v == 0.5 ? (Math.floor(v) % 2 == 0 ? Math.floor(v) : Math.ceil(v)) : Math.round(v));
  }
}
