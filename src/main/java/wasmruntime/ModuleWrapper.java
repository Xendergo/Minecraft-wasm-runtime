package wasmruntime;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import net.minecraft.server.MinecraftServer;
import wasmruntime.Exceptions.WasmtimeEmbeddingException;
import wasmruntime.Imports.Import;
import wasmruntime.Types.FuncSignature;
import wasmruntime.Types.Value;
import wasmruntime.Utils.DetectOS;
import wasmruntime.Utils.ImportCallCtx;
import wasmruntime.Utils.NativeUtils;

public class ModuleWrapper {
    public String moduleName;

    private long moduleId;

    public Map<String, FuncSignature> exportedFunctions = new HashMap<>();

    public Map<String, FuncSignature> importedFunctionSignatures = new HashMap<>();

    public Map<String, Function<ImportCallCtx, Value<?>[]>> importedFunctions = new HashMap<>();

    public MinecraftServer server;

    // <name, default>
    public static Map<String, Value<?>> knownSettings = new HashMap<>();

    static {
        try {
            String fileName;

            // for some reason my naming conventions are all over the place
            // but that's ok
            // psssstt... DetectOS is some code that was copied from stackoverflow
            // duh it has a stack overflow link -xendergo
            switch (DetectOS.getOperatingSystemType()) {
                case Windows:
                    fileName = "Wasmtime_embedding.dll";
                    break;

                case MacOS:
                    fileName = "Wasmtime_embedding.dylib";
                    break;

                case Linux:
                    fileName = "Wasmtime_embedding.so";
                    break;

                default:
                    throw new RuntimeException("Your OS is unsupported right now");
            }
            // Uses the NativeUtils library because dependency's are hard?
            NativeUtils.loadLibraryFromJar("/" + fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Set's autoReload to false which is apparently the default value according to
        // Xendergo
        // this doesn't set anything this is literally just a default value -xendergo
        knownSettings.put("autoReload", Value.fromI32(0));
    }

    // Wraps on a module like a day old piece of spaghettttt / Constructor sometimes
    // I think
    public ModuleWrapper(MinecraftServer server, File file, String name) throws WasmtimeEmbeddingException {
        moduleName = name;
        moduleId = LoadModule(file.getAbsolutePath(), importedFunctionSignatures);
        this.server = server;

        for (var entry : importedFunctionSignatures.entrySet()) {
            Import correctOverload = null;
            if (!ModuleImports.imports.containsKey(entry.getKey())) {
                // Maybe something will provide the import dynamically later (`provide_import`)
                continue;
            }

            for (var overload : ModuleImports.imports.get(entry.getKey())) {
                if (overload.test(entry.getValue())) {
                    if (correctOverload == null) {
                        correctOverload = overload;
                    } else {
                        throw new WasmtimeEmbeddingException(
                                "The overload of " + entry.getKey() + " to use is ambiguous");
                    }
                }
            }

            if (correctOverload != null) {
                importedFunctions.put(entry.getKey(), correctOverload.getFunction());
            } else {
                throw new WasmtimeEmbeddingException(
                        "No overload of " + entry.getKey() + " uses that function signature");
            }
        }

        for (var entry : ExportedFunctions(moduleId).entrySet()) {
            exportedFunctions.put(entry.getKey(), new FuncSignature(entry.getValue()));
        }
    }

    public List<Value<?>> CallExport(String name, List<Value<?>> params) throws WasmtimeEmbeddingException {
        return CallExport(moduleId, name, params);
    }

    public Value<?>[] CallExport(String name, Value<?>[] params) throws WasmtimeEmbeddingException {
        return CallExport(moduleId, name, Arrays.asList(params)).toArray(new Value<?>[0]);
    }

    public Value<?> GetSetting(String name) {
        throw new RuntimeException("Todo");
        // return GetGlobal(name, knownSettings.get(name));
    }

    // public Value<?> GetGlobal(String name, Value<?> defaultValue) {
    // try {
    // return GetGlobal(moduleId, name);
    // } catch (WasmtimeException e) {
    // return defaultValue;
    // }
    // }

    // public Value<?> GetGlobal(String name) throws WasmtimeException {
    // return GetGlobal(moduleId, name);
    // }

    public long Id() {
        return moduleId;
    }

    public void close() {
        UnloadModule(moduleId);
    }

    private static native long LoadModule(String path, Map<String, FuncSignature> importedFunctions)
            throws WasmtimeEmbeddingException;

    private static native void UnloadModule(long moduleId);

    private static native Map<String, List<Byte>> ExportedFunctions(long moduleId) throws WasmtimeEmbeddingException;

    private static native List<Value<?>> CallExport(long moduleId, String name, List<Value<?>> params)
            throws WasmtimeEmbeddingException;

    // all globals in the module
    private static native Map<String, Byte> Globals(long moduleId) throws WasmtimeEmbeddingException;
}
