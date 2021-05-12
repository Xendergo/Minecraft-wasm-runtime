package wasmruntime.Imports;

import java.util.function.Function;
import java.util.function.Predicate;

import wasmruntime.Enums.WasmType;
import wasmruntime.Types.Value;
import wasmruntime.Utils.ImportCallCtx;

public class Import {
  public Function<ImportCallCtx, Value<?>[]> function;
  public Predicate<WasmType[]> argsAllowed;

  public Import(Function<ImportCallCtx, Value<?>[]> function, Predicate<WasmType[]> argsAllowed) {
    this.function = function;
    this.argsAllowed = argsAllowed;
  }
}
