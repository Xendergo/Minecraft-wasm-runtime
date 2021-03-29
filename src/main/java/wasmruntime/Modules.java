package wasmruntime;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;

import wasmruntime.Exceptions.WasmtimeException;

public class Modules {
  public static Map<String, ModuleWrapper> modules = new HashMap<String, ModuleWrapper>();
  public static void LoadModule(FileObject path) throws IOException, WasmtimeException {
    String name = FilenameUtils.getBaseName(path.getName().getBaseName());

    UnloadModule(name);

    modules.put(name, new ModuleWrapper(path, name));
  }

  public static void UnloadModule(String name) {
    modules.remove(name);
  }
}
