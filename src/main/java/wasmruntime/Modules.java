package wasmruntime;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import net.minecraft.server.MinecraftServer;
import wasmruntime.Exceptions.WasmtimeEmbeddingException;

public class Modules {
    private Modules() {
    }

    static Map<String, ModuleWrapper> loadedModules = new HashMap<>();

    public static void LoadModule(MinecraftServer server, File path, String name) throws WasmtimeEmbeddingException {
        UnloadModule(name);

        loadedModules.put(name, new ModuleWrapper(server, path, name));
    }

    public static void LoadModule(MinecraftServer server, String name) throws WasmtimeEmbeddingException {
        var maybeFile = new File(WasmRuntime.configFolder, name + ".wasm");

        if (maybeFile.isFile()) {
            LoadModule(server, maybeFile, name);
        } else {
            LoadModule(server, new File(WasmRuntime.configFolder,
                    name + "/target/wasm32-wasi/debug/" + name.replace("-", "_") + ".wasm"), name);
        }
    }

    public static void UnloadModule(String name) {
        if (loadedModules.containsKey(name)) {
            loadedModules.get(name).close();
        }

        loadedModules.remove(name);
    }

    public static boolean moduleExists(String name) {
        return loadedModules.containsKey(name);
    }

    public static ModuleWrapper getModule(String name) {
        return loadedModules.get(name);
    }

    public static Set<String> allModules() {
        return loadedModules.keySet();
    }
}
