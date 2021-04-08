package wasmruntime.Utils;

import wasmruntime.Types.FuncType;
import wasmruntime.Types.Value;

public class ImportCallCtx {
  public String InstancePtr;
  public Value<?>[] values;
  public FuncType expectedType;

  public ImportCallCtx(Value<?>[] values, String ModuleName, FuncType expectedType) {
    this.values = values;
    this.InstancePtr = ModuleName;
    this.expectedType = expectedType;
  }
}
