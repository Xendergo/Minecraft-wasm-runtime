package wasmruntime.Operations.Math;

import wasmruntime.ModuleExecutor.ValueI32;
import wasmruntime.ModuleExecutor.ValueI64;
import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class Remainder {
  public static void i32_s(ValueStack stack) {
    int b = i32(stack);
    int a = i32(stack);
    stack.push(new ValueI32(a % b));
  }

  public static void i32_u(ValueStack stack) {
    int b = i32(stack);
    int a = i32(stack);
    stack.push(new ValueI32(Integer.remainderUnsigned(a, b)));
  }

  public static void i64_s(ValueStack stack) {
    Long b = i64(stack);
    Long a = i64(stack);
    stack.push(new ValueI64(a % b));
  }

  public static void i64_u(ValueStack stack) {
    Long b = i64(stack);
    Long a = i64(stack);
    stack.push(new ValueI64(Long.remainderUnsigned(a, b)));
  }
}
