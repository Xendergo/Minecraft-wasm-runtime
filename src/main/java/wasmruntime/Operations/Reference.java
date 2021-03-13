package wasmruntime.Operations;

import wasmruntime.ModuleExecutor.ValueI32;
import wasmruntime.ModuleExecutor.ValueFuncref;
import wasmruntime.ModuleExecutor.Value;
import wasmruntime.ModuleExecutor.ValueExternref;
import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class Reference {
  public static void nullRef(ValueStack stack) {
    switch (currentTypeAnnotation) {
      default:
      case funcref:
      stack.push(new ValueFuncref(-1));
      break;

      case externref:
      stack.push(new ValueExternref(-1));
      break;
    }
  }

  public static void isNull(ValueStack stack) {
    Value v = stack.pop();

    switch (currentTypeAnnotation) {
      default:
      case funcref:
      stack.push(new ValueI32(funcref(v) == -1 ? 1 : 0));
      break;

      case externref:
      stack.push(new ValueI32(externref(v) == -1 ? 1 : 0));
      break;
    }
  }

  public static void func(ValueStack stack) {
    stack.push(new ValueFuncref(i32(immediates[0])));
  }
}
