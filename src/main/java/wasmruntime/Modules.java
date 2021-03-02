package wasmruntime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import wasmruntime.Errors.WasmParseError;
import wasmruntime.Errors.WasmValidationError;
import wasmruntime.ModuleParsing.Parser;
import wasmruntime.ModuleData.Module;

public class Modules {
  public static Map<String, Module> modules = new HashMap<String, Module>();
  public static void LoadModule(File path, String name) throws IOException, WasmParseError, WasmValidationError {
    FileInputStream stream = new FileInputStream(path);

    byte[] bytes = stream.readAllBytes();

    stream.close();

    Module module = Parser.parseModule(bytes);

    module.IsValid();

    modules.put(name, module);

    System.out.println(modules.get(name));
  }
}
