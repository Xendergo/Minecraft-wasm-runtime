package net.fabricmc.wasmruntime.ModuleData;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.wasmruntime.ModuleData.HelpfulEnums.WasmType;

public class FunctionType {
  public List<WasmType> inputs;
  public List<WasmType> outputs;

  public FunctionType(List<WasmType> in, List<WasmType> out) {
    inputs = in;
    outputs = out;
  }

  public FunctionType(WasmType out) {
    inputs = new ArrayList<WasmType>();
    outputs = new ArrayList<WasmType>();
    outputs.add(out);
  }

  public FunctionType() {
    inputs = new ArrayList<WasmType>();
    outputs = new ArrayList<WasmType>();
  }

  public String toString() {
    return inputs + "=>" + outputs;
  }
}
