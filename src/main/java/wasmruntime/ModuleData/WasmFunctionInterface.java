package wasmruntime.ModuleData;

import wasmruntime.Errors.Trap;
import wasmruntime.ModuleExecutor.ValueStack;

public abstract class WasmFunctionInterface {
  public FunctionType type;
  public abstract int getStackSize();
  public abstract void Exec(ValueStack stack) throws Trap;
}
