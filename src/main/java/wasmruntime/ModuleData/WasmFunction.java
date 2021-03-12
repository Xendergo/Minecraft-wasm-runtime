package wasmruntime.ModuleData;

import wasmruntime.Errors.Trap;
import wasmruntime.ModuleExecutor.ExecExpression;
import wasmruntime.ModuleExecutor.Value;
import wasmruntime.ModuleExecutor.ValueStack;

public class WasmFunction extends WasmFunctionInterface {
  public Code code;

  public WasmFunction(Code codeOof) {
    code = codeOof;
    type = code.expr.type;
  }

  @Override
  public ValueStack Exec(Value[] stack, Module module) throws Trap {
    Value[] locals = new Value[code.locals.size() + code.expr.type.inputs.length];
    System.arraycopy(stack, 0, locals, 0, stack.length);
    return ExecExpression.Exec(code.expr, module, locals);
  }

  @Override
  public String toString() {
    return "WasmFunction {type: " + type + ", code: " + code + "}";
  }

  @Override
  public int getStackSize() {
    return code.expr.stackSize;
  }
}
