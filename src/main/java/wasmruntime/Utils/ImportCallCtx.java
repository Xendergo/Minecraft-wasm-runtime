package wasmruntime.Utils;

import wasmruntime.Types.Value;

public class ImportCallCtx {
  public long InstancePtr;
  public Value<?>[] values;

  public ImportCallCtx(Value<?>[] values, long InstancePtr) {
    this.values = values;
    this.InstancePtr = InstancePtr;
  }
}
