package wasmruntime.Utils;

import wasmruntime.Types.Value;

public class ImportCallCtx {
  public String InstancePtr;
  public Value<?>[] values;

  public ImportCallCtx(Value<?>[] values, String ModuleName) {
    this.values = values;
    this.InstancePtr = ModuleName;
  }
}
