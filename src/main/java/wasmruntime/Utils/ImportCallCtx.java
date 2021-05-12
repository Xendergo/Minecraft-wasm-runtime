package wasmruntime.Utils;

import wasmruntime.ModuleWrapper;
import wasmruntime.Types.FuncType;
import wasmruntime.Types.Value;

public class ImportCallCtx {
  public String InstancePtr;
  public Value<?>[] values;
  public FuncType expectedType;
  public ModuleWrapper Module;

  public ImportCallCtx(Value<?>[] values, String ModuleName, FuncType expectedType, ModuleWrapper Module) {
    this.values = values;
    this.InstancePtr = ModuleName;
    this.expectedType = expectedType;
    this.Module = Module;
  }
}
