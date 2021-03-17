package wasmruntime.Operations.Math;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

import wasmruntime.Errors.TrapRuntime;

public class Remainder {
  public static void i32_s(ValueStack stack) {
    int b = i32(stack);
    if (b == 0) throw new TrapRuntime("Divide by zero error");
    stack.push(i32(stack) % b);
  }

  public static void i32_u(ValueStack stack) {
    int b = i32(stack);
    if (b == 0) throw new TrapRuntime("Divide by zero error");
    stack.push(Integer.remainderUnsigned(i32(stack), b));
  }

  public static void i64_s(ValueStack stack) {
    Long b = i64(stack);
    if (b == 0) throw new TrapRuntime("Divide by zero error");
    stack.push(i64(stack) % b);
  }

  public static void i64_u(ValueStack stack) {
    Long b = i64(stack);
    if (b == 0) throw new TrapRuntime("Divide by zero error");
    stack.push(Long.remainderUnsigned(i64(stack), b));
  }
}
