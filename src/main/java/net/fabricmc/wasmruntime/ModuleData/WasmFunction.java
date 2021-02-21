package net.fabricmc.wasmruntime.ModuleData;

public class WasmFunction {
   public int typeIndex = -1;
  public int codeIndex = -1;

  public WasmFunction(int typeIndexOof) {
    typeIndex = typeIndexOof;
  }

  public String toString() {
    return "Function {typeIndex: "+typeIndex+", codeIndex: "+codeIndex+"}";
  }
}
