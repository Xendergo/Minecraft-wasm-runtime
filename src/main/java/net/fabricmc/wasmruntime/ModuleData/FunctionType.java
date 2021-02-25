package net.fabricmc.wasmruntime.ModuleData;

import net.fabricmc.wasmruntime.ModuleData.HelpfulEnums.WasmType;

public class FunctionType {
  public WasmType[] inputs;
  public WasmType[] outputs;

  public FunctionType(WasmType[] in, WasmType[] out) {
    inputs = in;
    outputs = out;
  }

  public FunctionType(WasmType out) {
    inputs = new WasmType[0];
    outputs = new WasmType[] {out};
  }

  public FunctionType() {
    inputs = new WasmType[0];
    outputs = new WasmType[0];
  }

  public String toString() {
    return inputs + "=>" + outputs;
  }
}
