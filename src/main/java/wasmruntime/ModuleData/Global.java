package wasmruntime.ModuleData;

import wasmruntime.ModuleData.HelpfulEnums.WasmType;
import wasmruntime.ModuleExecutor.Value;

public class Global {
  private Value value;
  public boolean mutable;
  public WasmType type;

  public Global(Value valueOof, boolean mutableOof, WasmType typeOof) {
    value = valueOof;
    mutable = mutableOof;
    type = typeOof;
  }

  public void setValue(Value newValue) {
    value = newValue;
  }

  public Value getValue() {
    return value;
  }

  public String toString() {
    return "Global: " + value + (mutable ? ", mutable" : "");
  }
}
