package wasmruntime.Types;

import java.util.ArrayList;
import java.util.List;

import wasmruntime.Enums.WasmType;

// type signature of a wasm function -matgenius04
public class FuncType {
  public WasmType[] inputs;
  public WasmType[] outputs;

  public FuncType(List<Byte> bytes) {
    List<WasmType> inputs = new ArrayList<WasmType>();
    List<WasmType> outputs = new ArrayList<WasmType>();

    for (Byte id : bytes) {
      List<WasmType> thingy = (id & 0x80) == 0 ? inputs : outputs;

      thingy.add(WasmType.idMap.get((byte) (id & 0x7F)));
    }

    this.inputs = inputs.toArray(new WasmType[0]);
    this.outputs = outputs.toArray(new WasmType[0]);
  }

  @Override
  public String toString() {
    return inputs + " => " + outputs;
  }
}
