package net.fabricmc.wasmruntime.ModuleExecutor;

import net.fabricmc.wasmruntime.ModuleData.Expression;

public class ExecExpression {
  // I'll do this later
  public static ValueStack Exec(Expression expr) {
    ValueStack stack = new ValueStack(1);
    stack.push(new ValueI32(0));
    return stack;
  }
}
