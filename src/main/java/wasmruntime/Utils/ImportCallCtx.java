package wasmruntime.Utils;

import wasmruntime.Enums.WasmType;
import wasmruntime.Types.Value;

public class ImportCallCtx {
  public long InstancePtr;
  public Value<?>[] values;
  public WasmType[] types;

  public ImportCallCtx(Value<?>[] values, WasmType[] types, long InstancePtr) {
    this.values = values;
    this.InstancePtr = InstancePtr;
    this.types = types;
  }
}
