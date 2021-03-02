package wasmruntime.ModuleExecutor;

import wasmruntime.ModuleData.HelpfulEnums.WasmType;

public interface Value {
  public WasmType type = WasmType.i32;

  public String toString();
}
