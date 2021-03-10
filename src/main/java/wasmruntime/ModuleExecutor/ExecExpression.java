package wasmruntime.ModuleExecutor;

import wasmruntime.Errors.Trap;
import wasmruntime.Errors.TrapRuntime;
import wasmruntime.ModuleData.Expression;
import wasmruntime.ModuleData.Module;
import wasmruntime.ModuleData.Opcodes;

public class ExecExpression {
  public static int branchDepth = -1;

  // TODO: this
  public static ValueStack Exec(Expression expr, Module module, Value[] locals) throws Trap {
    Instruction[] instructions = expr.bytecode;
    ValueStack stack = new ValueStack(expr.stackSize, module);
    expr.locals = locals;

    for (int i = 0; i < instructions.length; i++) {
      try {
        Opcodes.immediates = instructions[i].immediates;
        instructions[i].operation.operation.accept(stack);
      } catch (TrapRuntime trap) {
        throw new Trap(trap.getMessage());
      }
    }
    
    return stack;
  }
}
