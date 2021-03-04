package wasmruntime.ModuleData;

import wasmruntime.Errors.Trap;
import wasmruntime.ModuleExecutor.ExecExpression;
import wasmruntime.ModuleExecutor.ValueStack;

public class WasmFunction extends WasmFunctionInterface {
  public Code code;

  public WasmFunction(Code codeOof) {
    code = codeOof;
    type = code.expr.type;
  }

  @Override
  public void Exec(ValueStack stack) throws Trap {
    ExecExpression.Exec(code.expr, stack.module);
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
