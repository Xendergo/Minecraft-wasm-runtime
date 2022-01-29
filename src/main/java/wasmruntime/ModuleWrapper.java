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
import wasmruntime.Enums.WasmType;
import wasmruntime.Exceptions.WasmtimeException;
import wasmruntime.Types.FuncType;
import wasmruntime.Types.Value;
import wasmruntime.Utils.DetectOS;
import wasmruntime.Utils.ImportCallCtx;
import wasmruntime.Utils.NativeUtils;

public class ModuleWrapper {
  public String moduleName;

  private long moduleId;
  
  public Map<String, FuncType> exportedFunctions = new HashMap<>();

  public Map<String, FuncType> importedFunctions = new HashMap<>();

  public Map<String, WasmType> exportedGlobals = new HashMap<>();

  public Map<String, Function<ImportCallCtx, Value<?>[]>> imports = new HashMap<>();

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
      NativeUtils.loadLibraryFromJar("/"+fileName);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    // Set's autoReload to false which is apparently the default value according to Xendergo
    // this doesn't set anything this is literally just a default value -xendergo
    knownSettings.put("autoReload", Value.fromI32(0));
  }
  
  // Wraps on a module like a day old piece of spaghettttt / Constructor sometimes I think
  public ModuleWrapper(MinecraftServer server, File file, String name) throws WasmtimeException {
    moduleName = name;
    moduleId = LoadModule(file.getAbsolutePath(), moduleName, importedFunctions);
    this.server = server;

    if (!ModuleImports.perModuleImports.containsKey(moduleName)) ModuleImports.perModuleImports.put(moduleName, new HashMap<>());
    
    for (Entry<String, List<Byte>> entry : ExportedFunctions(moduleId).entrySet()) {
      exportedFunctions.put(entry.getKey(), new FuncType(entry.getValue()));
    }

    for (Entry<String, Byte> entry : Globals(moduleId).entrySet()) {
      exportedGlobals.put(entry.getKey(), WasmType.idMap.get(entry.getValue()));
    }
  }

  public List<Value<?>> CallExport(String name, List<Value<?>> params) throws WasmtimeException {
    return CallExport(moduleId, name, params);
  }

  public Value<?>[] CallExport(String name, Value<?>[] params) throws WasmtimeException {
    return CallExport(moduleId, name, Arrays.asList(params)).toArray(new Value<?>[0]);
  }

  public Value<?> GetSetting(String name) {
    return GetGlobal(name, knownSettings.get(name));
  }

  public Value<?> GetGlobal(String name, Value<?> defaultValue) {
    try {
      return GetGlobal(moduleId, name);
    } catch (WasmtimeException e) {
      return defaultValue;
    }
  }

  public Value<?> GetGlobal(String name) throws WasmtimeException {
    return GetGlobal(moduleId, name);
  }
  
  public void close() {
    UnloadModule(moduleId);
  }

  private static native long LoadModule(String path, String name, Map<String, FuncType> importedFunctions) throws WasmtimeException;

  private static native void UnloadModule(long moduleId);

  private static native Map<String, List<Byte>> ExportedFunctions(long moduleId) throws WasmtimeException;

  private static native List<Value<?>> CallExport(long moduleId, String name, List<Value<?>> params) throws WasmtimeException;

  // all globals in the module
  private static native Map<String, Byte> Globals(long moduleId) throws WasmtimeException;

  private static native Value<?> GetGlobal(long moduleId, String name) throws WasmtimeException;
}
