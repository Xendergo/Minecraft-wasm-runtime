package wasmruntime.Utils;

import wasmruntime.ModuleWrapper;
import wasmruntime.Types.FuncSignature;
import wasmruntime.Types.Value;

public class ImportCallCtx {
    public String InstancePtr;
    public Value<?>[] values;
    public FuncSignature expectedType;
    public ModuleWrapper Module;

    public ImportCallCtx(Value<?>[] values, String ModuleName, FuncSignature expectedType, ModuleWrapper Module) {
        this.values = values;
        this.InstancePtr = ModuleName;
        this.expectedType = expectedType;
        this.Module = Module;
    }
}
