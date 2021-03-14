package wasmruntime.ModuleData;

import wasmruntime.ModuleData.HelpfulEnums.ElementTypes;
import wasmruntime.ModuleData.HelpfulEnums.WasmType;
import wasmruntime.ModuleExecutor.Value;

public class Element {
  public Value[] data;
  WasmType type;
  ElementTypes usage;

  public Element(Value[] dataOof, WasmType typeOof, ElementTypes usageOof) {
    data = dataOof;
    type = typeOof;
    usage = usageOof;
  }
}
