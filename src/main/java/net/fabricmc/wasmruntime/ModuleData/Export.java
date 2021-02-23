package net.fabricmc.wasmruntime.ModuleData;

import net.fabricmc.wasmruntime.ModuleData.HelpfulEnums.ExportTypes;

public class Export {
  public ExportTypes type;
  public int index;

  public Export(ExportTypes typeOof, int indexOof) {
    type = typeOof;
    index = indexOof;
  }

  public String toString() {
    return "Export {type: " + type + ", index: " + index + "}";
  }
}
