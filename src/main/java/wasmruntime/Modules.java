package wasmruntime;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import net.minecraft.server.MinecraftServer;
import wasmruntime.Exceptions.WasmtimeException;

public class Modules {
  public static Map<String, ModuleWrapper> modules = new HashMap<>();
  public static MinecraftServer server;

  public static void LoadModule(File path) throws WasmtimeException {
    String name = FilenameUtils.getBaseName(path.getName());

    UnloadModule(name);

    modules.put(name, new ModuleWrapper(path, name));
  }

  public static void LoadModule(String name) throws WasmtimeException {
    var maybeFile = new File(WasmRuntime.configFolder, name + ".wasm");

    if (maybeFile.isFile()) {
      LoadModule(maybeFile);
    } else {
      LoadModule(new File(WasmRuntime.configFolder, name + "/target/wasm32-wasi/debug/" + name.replace("-", "_") + ".wasm"));
    }
  }

  public static void UnloadModule(String name) {
    if (modules.containsKey(name)) {
      modules.get(name).close();
    }

    modules.remove(name);
  }
}
