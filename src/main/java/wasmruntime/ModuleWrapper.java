package wasmruntime;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import wasmruntime.Enums.WasmType;
import wasmruntime.Exceptions.WasmtimeException;
import wasmruntime.Types.FuncType;
import wasmruntime.Types.Value;
import wasmruntime.Utils.DetectOS;
import wasmruntime.Utils.ImportCallCtx;
import wasmruntime.Utils.NativeUtils;

public class ModuleWrapper {
  public static final boolean debug = true;
  public String moduleName;

  private long InstanceID;
  
  public Map<String, FuncType> exportedFunctions = new HashMap<String, FuncType>();

  public Map<String, FuncType> importedFunctions = new HashMap<String, FuncType>();

  public Map<String, WasmType> exportedGlobals = new HashMap<String, WasmType>();

  public Map<String, Function<ImportCallCtx, Value<?>[]>> imports = new HashMap<String, Function<ImportCallCtx, Value<?>[]>>();

  // <name, default>
  public static Map<String, Value<?>> knownSettings = new HashMap<String, Value<?>>();

  static {
    try {
      String fileName = "";

      if (debug) {
        // because I'm a window's simp
        fileName = "debug/Wasmtime_embedding.dll";
      } else {
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
      }
      // Uses the NativeUtils library because dependency's are hard?
      NativeUtils.loadLibraryFromJar("/"+fileName);
      // Initializes the things?
      Init();
    } catch (WasmtimeException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    // Set's autoReload to false which is apparently the default value according to Xendergo
    // this doesn't set anything this is literally just a default value -xendergo
    knownSettings.put("autoReload", Value.fromI32(0));
  }
  // Wraps on a module like a day old piece of spaghettttt / Constructor sometimes I think
  public ModuleWrapper(File file, String name) throws IOException, WasmtimeException {
    moduleName = name;
    InstanceID = LoadModule(file.getAbsolutePath(), moduleName, importedFunctions);

    if (!ModuleImports.perModuleImports.containsKey(moduleName)) ModuleImports.perModuleImports.put(moduleName, new HashMap<String, Function<ImportCallCtx, Value<?>[]>>());
    
    for (Entry<String, List<Byte>> entry : Functions(InstanceID).entrySet()) {
      exportedFunctions.put(entry.getKey(), new FuncType(entry.getValue()));
    }

    for (Entry<String, Byte> entry : Globals(InstanceID).entrySet()) {
      exportedGlobals.put(entry.getKey(), WasmType.idMap.get(entry.getValue()));
    }
  }

  public List<Value<?>> CallFunction(String name, List<Value<?>> params) throws WasmtimeException {
    return CallFunction(InstanceID, name, params);
  }

  public Value<?>[] CallFunction(String name, Value<?>[] params) throws WasmtimeException {
    return CallFunction(InstanceID, name, Arrays.asList(params)).toArray(new Value<?>[0]);
  }

  public Value<?> GetSetting(String name) {
    return GetGlobal(name, knownSettings.get(name));
  }

  public Value<?> GetGlobal(String name, Value<?> defaultValue) {
    try {
      return GetGlobal(InstanceID, name);
    } catch (WasmtimeException e) {
      return defaultValue;
    }
  }

  public Value<?> GetGlobal(String name) throws WasmtimeException {
    return GetGlobal(InstanceID, name);
  }

  public String ReadString(List<Long> ptr) throws WasmtimeException {
    return ReadString(InstanceID, ptr);
  }
  
  public void close() {
    UnloadModule(InstanceID);
  }

  private static native long LoadModule(String path, String name, Map<String, FuncType> importedFunctions) throws WasmtimeException;

  private static native void Init() throws WasmtimeException;

  private static native void UnloadModule(long InstancePtr);

  // all functions in the module
  private static native Map<String, List<Byte>> Functions(long InstancePtr) throws WasmtimeException;

  private static native List<Value<?>> CallFunction(long InstancePtr, String name, List<Value<?>> params) throws WasmtimeException;

  // all globals in the module
  private static native Map<String, Byte> Globals(long InstancePtr) throws WasmtimeException;

  private static native Value<?> GetGlobal(long InstancePtr, String name) throws WasmtimeException;

  private static native String ReadString(long InstancePtr, List<Long> ptr) throws WasmtimeException;
}
