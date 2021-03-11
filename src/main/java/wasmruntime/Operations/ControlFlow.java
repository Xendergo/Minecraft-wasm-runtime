package wasmruntime.Operations;

import wasmruntime.ModuleExecutor.ExecExpression;
import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class ControlFlow {
  public static void branch(ValueStack stack) {
    ExecExpression.branchOutput = stack;
    ExecExpression.branchDepth = i32(immediates[0]);
  }

  public static void branchIf(ValueStack stack) {
    if (i32(stack.pop()) == 0) {
      ExecExpression.branchOutput = stack;
      ExecExpression.branchDepth = i32(immediates[0]);
    }
  }

  public static void branchTable(ValueStack stack) {
    int index = i32(stack);
    ExecExpression.branchOutput = stack;
    if (immediates.length <= index) {
      ExecExpression.branchDepth = i32(immediates[immediates.length - 1]);
    } else {
      ExecExpression.branchDepth = i32(immediates[index]);
    }
  }
}
