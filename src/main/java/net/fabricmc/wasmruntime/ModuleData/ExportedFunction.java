package net.fabricmc.wasmruntime.ModuleData;

public class ExportedFunction {
  public String name;
  public int funcId;

  public ExportedFunction(String nameOof, int funcIdOof) {
    name = nameOof;
    funcId = funcIdOof;
  }

  public String toString() {
    return "ExportedFunction {name: "+name+", funcId: "+funcId+"}";
  }
}
