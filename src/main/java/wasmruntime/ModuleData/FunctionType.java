package wasmruntime.ModuleData;

import wasmruntime.ModuleData.HelpfulEnums.WasmType;
import wasmruntime.ModuleExecutor.Value;
import wasmruntime.ModuleExecutor.ValueStack;

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

  public FunctionType(FunctionType type) {
    inputs = new WasmType[type.inputs.length];
    outputs = new WasmType[type.outputs.length];

    System.arraycopy(type.inputs, 0, inputs, 0, type.inputs.length);
    System.arraycopy(type.outputs, 0, outputs, 0, type.outputs.length);
  }

  public Value[] popArgs(ValueStack stack) {
    Value[] locals = new Value[inputs.length];

    for (int i = 0; i < locals.length; i++) {
      locals[i] = stack.pop();
    }

    return locals;
  }

  public String toString() {
    return inputs + "=>" + outputs;
  }
}
