package wasmruntime.ModuleExecutor;

import wasmruntime.Errors.Trap;
import wasmruntime.Errors.TrapRuntime;
import wasmruntime.ModuleData.Expression;
import wasmruntime.ModuleData.Module;
import wasmruntime.ModuleData.Opcodes;

public class ExecExpression {
  public static int branchDepth = -1;
  public static ValueStack branchOutput;

  public static ValueStack Exec(Expression expr, Module module, Value[] locals) throws Trap {
    return Exec(expr, module, locals, new ValueStack(expr.stackSize, module));
  }
  
  public static ValueStack Exec(Expression expr, Module module, Value[] locals, ValueStack stackInitializer) throws Trap {
    Instruction[] instructions = expr.bytecode;
    ValueStack stack = stackInitializer;
    expr.locals = locals;
  
    for (int i = 0; i < instructions.length; i++) {
      try {
        Opcodes.immediates = instructions[i].immediates;
        instructions[i].operation.operation.accept(stack);
        if (branchDepth != -1) {
          if (branchDepth == 0) {
            stack.pushStack(expr.type.popOutput(branchOutput));
            branchOutput = null;
          }

          i = expr.isLoop && branchDepth == 0 ? -1 /*1 will be incremented by the for loop*/ : instructions.length - 1;

          branchDepth--;
        }
      } catch (TrapRuntime trap) {
        throw new Trap(trap.getMessage());
      }
    }
    
    return stack;
  }
}
