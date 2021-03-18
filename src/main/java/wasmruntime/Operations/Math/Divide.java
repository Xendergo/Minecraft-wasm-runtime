package wasmruntime.Operations.Math;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

import wasmruntime.Errors.TrapRuntime;

public class Divide {
  public static void I32_s(ValueStack stack) {
    int b = i32(stack);
    if (b == 0) throw new TrapRuntime("Divide by zero error");
    stack.push(i32(stack) / b);
  }

  public static void I32_u(ValueStack stack) {
    int b = i32(stack);
    if (b == 0) throw new TrapRuntime("Divide by zero error");
    stack.push(Integer.divideUnsigned(i32(stack), b));
  }

  public static void I64_s(ValueStack stack) {
    Long b = i64(stack);
    if (b == 0) throw new TrapRuntime("Divide by zero error");
    stack.push(i64(stack) / b);
  }

  public static void I64_u(ValueStack stack) {
    Long b = i64(stack);
    if (b == 0) throw new TrapRuntime("Divide by zero error");
    stack.push(Long.divideUnsigned(i64(stack), b));
  }

  public static void F32(ValueStack stack) {
    float b = f32(stack);
    stack.push(f32(stack) / b);
  }

  public static void F64(ValueStack stack) {
    double b = f64(stack);
    stack.push(f64(stack) / b);
  }
}