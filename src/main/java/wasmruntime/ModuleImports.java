package wasmruntime;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import wasmruntime.Types.Value;
import wasmruntime.Utils.ImportCallCtx;

class ModuleImports {
  public static Map<String, Function<ImportCallCtx, Value<?>[]>> imports = new HashMap<String, Function<ImportCallCtx, Value<?>[]>>();

  public static Value<?>[] callImport(String name, Value<?>[] values, long InstancePtr) {
    return imports.get(name).apply(new ImportCallCtx(values, InstancePtr));
  }

  public static void Register(String name, Function<ImportCallCtx, Value<?>[]> fn) {
    if (imports.containsKey(name)) throw new RuntimeException("There's already an import with the name " + name);

    imports.put(name, fn);
  }
}