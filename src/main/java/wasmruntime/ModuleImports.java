package wasmruntime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import wasmruntime.Imports.Import;
import wasmruntime.Types.FuncSignature;
import wasmruntime.Types.Value;
import wasmruntime.Utils.ImportCallCtx;

public class ModuleImports {
    public static Map<String, List<Import>> imports = new HashMap<>();

    public static Value<?>[] callImport(String name, Value<?>[] values, long moduleId) {
        String moduleName = null;

        for (var entry : Modules.loadedModules.entrySet()) {
            if (entry.getValue().Id() == moduleId) {
                moduleName = entry.getKey();
            }
        }

        ModuleWrapper module = Modules.loadedModules.get(moduleName);

        return module.importedFunctions.get(name)
                .apply(new ImportCallCtx(values, moduleName, module.importedFunctionSignatures.get(name), module));
    }

    public static void Register(String name, Function<ImportCallCtx, Value<?>[]> fn) {
        imports.computeIfAbsent(name, v -> new ArrayList<>());

        imports.get(name).add(new Import(fn, v -> true));
    }

    public static void Register(String name, Function<ImportCallCtx, Value<?>[]> fn, FuncSignature funcSignature) {
        imports.computeIfAbsent(name, v -> new ArrayList<>());

        imports.get(name).add(new Import(fn, v -> {
            if (funcSignature.inputs.length != v.inputs.length)
                return false;
            if (funcSignature.outputs.length != v.outputs.length)
                return false;

            for (int i = 0; i < v.inputs.length; i++) {
                if (v.inputs[i] != funcSignature.inputs[i])
                    return false;
            }

            for (int i = 0; i < v.outputs.length; i++) {
                if (v.outputs[i] != funcSignature.outputs[i])
                    return false;
            }

            return true;
        }));
    }

    public static void Register(String name, Function<ImportCallCtx, Value<?>[]> fn,
            Predicate<FuncSignature> argsValid) {
        imports.computeIfAbsent(name, v -> new ArrayList<>());

        imports.get(name).add(new Import(fn, argsValid));
    }
}