package wasmruntime.ModuleData;

import wasmruntime.Errors.Trap;
import wasmruntime.ModuleExecutor.Value;
import wasmruntime.ModuleExecutor.ValueStack;

public abstract class WasmFunctionInterface {
  public FunctionType type;
  public abstract int getStackSize();
  public abstract ValueStack Exec(Value[] stack, Module module) throws Trap;
  public Value[] popArgs(ValueStack stack) {
    Value[] locals = new Value[type.inputs.length];

    for (int i = 0; i < locals.length; i++) {
      locals[i] = stack.pop();
    }

    return locals;
  }
}
