package wasmruntime;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;

import wasmruntime.Errors.WasmParseError;
import wasmruntime.Errors.WasmValidationError;
import wasmruntime.ModuleParsing.Parser;
import wasmruntime.ModuleData.Module;

public class Modules {
  public static Map<String, Module> modules = new HashMap<String, Module>();
  public static void LoadModule(FileObject path) throws IOException, WasmParseError, WasmValidationError {
    String name = FilenameUtils.getBaseName(path.getName().getBaseName());

    UnloadModule(name);

    byte[] bytes = path.getContent().getByteArray();

    Module module = Parser.parseModule(bytes);

    module.IsValid();

    modules.put(name, module);
  }

  public static void UnloadModule(String name) {
    modules.remove(name);
  }
}
