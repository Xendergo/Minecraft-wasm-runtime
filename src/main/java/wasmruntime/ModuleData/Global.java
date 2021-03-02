package wasmruntime.ModuleData;

import wasmruntime.ModuleData.HelpfulEnums.WasmType;
import wasmruntime.ModuleExecutor.Value;

public class Global<T extends Value> {
  private T value;
  public boolean mutable;
  public WasmType type;

  public Global(T valueOof, boolean mutableOof, WasmType typeOof) {
    value = valueOof;
    mutable = mutableOof;
    type = typeOof;
  }

  public T getValue() {
    return value;
  }

  public String toString() {
    return "Global: " + value + (mutable ? ", mutable" : "");
  }
}
