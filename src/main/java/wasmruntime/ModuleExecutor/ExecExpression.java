package wasmruntime.ModuleExecutor;

import wasmruntime.ModuleData.Expression;

public class ExecExpression {
  public static int branchDepth = -1;

  // TODO: this
  public static ValueStack Exec(Expression expr) {
    ValueStack stack = new ValueStack(1);
    stack.push(new ValueI32(0));
    return stack;
  }
}
