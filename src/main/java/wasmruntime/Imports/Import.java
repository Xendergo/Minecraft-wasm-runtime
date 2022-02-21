package wasmruntime.Imports;

import java.util.function.Function;
import java.util.function.Predicate;

import wasmruntime.Types.FuncSignature;
import wasmruntime.Types.Value;
import wasmruntime.Utils.ImportCallCtx;

public class Import {
    Function<ImportCallCtx, Value<?>[]> function;
    Predicate<FuncSignature> argsAllowed;

    public Import(Function<ImportCallCtx, Value<?>[]> function, Predicate<FuncSignature> argsAllowed) {
        this.function = function;
        this.argsAllowed = argsAllowed;
    }

    public boolean test(FuncSignature signature) {
        return argsAllowed.test(signature);
    }

    public Function<ImportCallCtx, Value<?>[]> getFunction() {
        return function;
    }
}
