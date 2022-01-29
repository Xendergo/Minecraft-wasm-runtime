package wasmruntime;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import wasmruntime.Enums.WasmType;
import wasmruntime.Imports.Import;
import wasmruntime.Types.Value;
import wasmruntime.Utils.ImportCallCtx;

public class ModuleImports {
  public static Map<String, Import> imports = new HashMap<String, Import>();

  public static Map<String, Map<String, Function<ImportCallCtx, Value<?>[]>>> perModuleImports = new HashMap<String, Map<String, Function<ImportCallCtx, Value<?>[]>>>();

  public static Value<?>[] callImport(String name, Value<?>[] values, String ModuleName) {
    ModuleWrapper module = Modules.loadedModules.get(ModuleName);

    if (perModuleImports.get(ModuleName).containsKey(name)) {
      return perModuleImports.get(ModuleName).get(name).apply(new ImportCallCtx(values, ModuleName, module.importedFunctions.get(name), module));
    } else {
      return imports.get(name).function.apply(new ImportCallCtx(values, ModuleName, module.importedFunctions.get(name), module));
    }
  }

  private static final String IMPORT_ERROR = "There's already an import with the name ";

  public static void Register(String name, Function<ImportCallCtx, Value<?>[]> fn) {
    if (imports.containsKey(name)) throw new RuntimeException(IMPORT_ERROR + name);

    imports.put(name, new Import(fn, (v) -> true));
  }

  public static void Register(String name, Function<ImportCallCtx, Value<?>[]> fn, WasmType[] typeSignature) {
    if (imports.containsKey(name)) throw new RuntimeException(IMPORT_ERROR + name);

    imports.put(name, new Import(fn, (v) -> {
      if (typeSignature.length != v.length) return false;

      for (int i = 0; i < v.length; i++) {
        if (v[i] != typeSignature[i]) return false;
      }

      return true;
    }));
  }

  public static void Register(String name, Function<ImportCallCtx, Value<?>[]> fn, Predicate<WasmType[]> argsValid) {
    if (imports.containsKey(name)) throw new RuntimeException(IMPORT_ERROR + name);

    imports.put(name, new Import(fn, argsValid));
  }
}