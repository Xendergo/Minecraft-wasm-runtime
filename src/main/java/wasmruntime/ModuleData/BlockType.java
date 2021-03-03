package wasmruntime.ModuleData;

import wasmruntime.ModuleData.HelpfulEnums.WasmType;

public class BlockType {
  public FunctionType type;
  boolean isLoop;

  public WasmType[] getResultType() {
    return isLoop ? type.inputs : type.outputs;
  }

  public BlockType(FunctionType typeOof, boolean isLoopOof) {
    type = typeOof;
    isLoop = isLoopOof;
  }

  public BlockType(FunctionType typeOof) {
    type = typeOof;
    isLoop = false;
  }
}
