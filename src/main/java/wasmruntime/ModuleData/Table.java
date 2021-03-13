package wasmruntime.ModuleData;

import java.util.HashMap;

import wasmruntime.ModuleData.HelpfulEnums.WasmType;

public class Table {
  public Limit limits;
  public WasmType type;

  public HashMap<Integer, Integer> values = new HashMap<Integer, Integer>();

  public Table(Limit limitsOof, WasmType typeOof) {
    limits = limitsOof;
    type = typeOof;
  }

  public boolean IsValid() {
    return limits.IsValid(2147483647);
  }

  public String toString() {
    return "Table {"+limits+", type: "+type+", values: " + values + "}";
  }
}
