package wasmruntime.Operations.Math;

import wasmruntime.ModuleExecutor.ValueF32;
import wasmruntime.ModuleExecutor.ValueF64;
import wasmruntime.ModuleExecutor.ValueI32;
import wasmruntime.ModuleExecutor.ValueI64;
import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class Add {
  public static void I32(ValueStack stack) {
    stack.push(new ValueI32(i32(stack) + i32(stack)));
  }

  public static void I64(ValueStack stack) {
    stack.push(new ValueI64(i64(stack) + i64(stack)));
  }

  public static void F32(ValueStack stack) {
    stack.push(new ValueF32(f32(stack) + f32(stack)));
  }

  public static void F64(ValueStack stack) {
    stack.push(new ValueF64(f64(stack) + f64(stack)));
  }
}
