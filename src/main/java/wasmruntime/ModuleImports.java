package wasmruntime;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import wasmruntime.Types.Value;
import wasmruntime.Utils.ImportCallCtx;

public class ModuleImports {
  public static Map<String, Function<ImportCallCtx, Value<?>[]>> imports = new HashMap<String, Function<ImportCallCtx, Value<?>[]>>();

  public static Map<String, Map<String, Function<ImportCallCtx, Value<?>[]>>> perModuleImports = new HashMap<String, Map<String, Function<ImportCallCtx, Value<?>[]>>>();

  public static Value<?>[] callImport(String name, Value<?>[] values, String ModuleName) {
    ModuleWrapper module = Modules.modules.get(ModuleName);

    if (perModuleImports.get(ModuleName).containsKey(name)) {
      return perModuleImports.get(ModuleName).get(name).apply(new ImportCallCtx(values, ModuleName, module.importedFunctions.get(name)));
    } else {
      return imports.get(name).apply(new ImportCallCtx(values, ModuleName, module.importedFunctions.get(name)));
    }
  }

  public static void Register(String name, Function<ImportCallCtx, Value<?>[]> fn) {
    if (imports.containsKey(name)) throw new RuntimeException("There's already an import with the name " + name);

    imports.put(name, fn);
  }
}