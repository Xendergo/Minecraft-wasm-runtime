package wasmruntime.ModuleExecutor;

import wasmruntime.ModuleData.Expression;
import wasmruntime.ModuleData.Module;

public class ExecExpression {
  public static int branchDepth = -1;

  // TODO: this
  public static ValueStack Exec(Expression expr, Module module) {
    ValueStack stack = new ValueStack(1, module);
    stack.push(new ValueI32(0));
    return stack;
  }
}
