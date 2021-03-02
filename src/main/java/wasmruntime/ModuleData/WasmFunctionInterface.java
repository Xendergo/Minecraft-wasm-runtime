package wasmruntime.ModuleData;

import wasmruntime.ModuleExecutor.ValueStack;

public abstract class WasmFunctionInterface {
  public FunctionType type;
  public abstract ValueStack Exec();
}
