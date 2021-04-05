package wasmruntime;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import net.minecraft.server.MinecraftServer;
import wasmruntime.Exceptions.WasmtimeException;

public class Modules {
  public static Map<String, ModuleWrapper> modules = new HashMap<String, ModuleWrapper>();
  public static MinecraftServer server;
  public static void LoadModule(File path) throws IOException, WasmtimeException {
    String name = FilenameUtils.getBaseName(path.getName());

    UnloadModule(name);

    modules.put(name, new ModuleWrapper(path, name));
  }

  public static void UnloadModule(String name) {
    if (modules.containsKey(name)) {
      modules.get(name).close();
    }

    modules.remove(name);
  }
}
