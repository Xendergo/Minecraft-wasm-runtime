package net.fabricmc.wasmruntime.ModuleData;

import net.fabricmc.wasmruntime.ModuleData.HelpfulEnums.ElementType;

public class WasmTable {
  public Limit limits;
  public ElementType type;

  public WasmTable(Limit limitsOof, ElementType typeOof) {
    limits = limitsOof;
    type = typeOof;
  }

  public String toString() {
    return "Table {"+limits+", type: "+type+"}";
  }
}
