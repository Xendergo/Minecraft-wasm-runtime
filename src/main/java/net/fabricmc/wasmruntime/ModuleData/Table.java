package net.fabricmc.wasmruntime.ModuleData;

import java.util.HashMap;

import net.fabricmc.wasmruntime.ModuleData.HelpfulEnums.ElementType;

public class Table {
  public Limit limits;
  public ElementType type;

  public HashMap<Integer, Integer> values = new HashMap<Integer, Integer>();

  public Table(Limit limitsOof, ElementType typeOof) {
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
