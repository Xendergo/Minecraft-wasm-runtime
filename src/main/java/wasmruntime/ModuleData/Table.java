package wasmruntime.ModuleData;

import wasmruntime.ModuleData.HelpfulEnums.WasmType;
import wasmruntime.ModuleExecutor.Value;

public class Table {
  public Limit limits;
  public WasmType type;

  public Value[] values = new Value[0];

  public Table(Limit limitsOof, WasmType typeOof) {
    limits = limitsOof;
    values = new Value[limits.min];
    type = typeOof;
  }

  public void Initialize(Element elem, int offset) {
    System.arraycopy(elem.data, 0, values, offset, elem.data.length);
  }

  public void Initialize(Element elem, int offset, int length, int start) {
    System.arraycopy(elem.data, start, values, offset, length);
  }

  public void Copy(Table table, int offset, int length, int start) {
    System.arraycopy(table.values, start, values, offset, length);
  }

  public boolean IsValid() {
    return limits.IsValid(2147483647);
  }

  public String toString() {
    return "Table {"+limits+", type: "+type+", values: " + values + "}";
  }
}
