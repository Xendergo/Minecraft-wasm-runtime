package wasmruntime.Operations.Conversions;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class PromoteDemote {
  public static void promote(ValueStack stack) {
    stack.push((double) f32(stack));
  }

  public static void demote(ValueStack stack) {
    stack.push((float) f64(stack));
  }
}
