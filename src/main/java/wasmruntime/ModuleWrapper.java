package wasmruntime;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import wasmruntime.Enums.WasmType;
import wasmruntime.Exceptions.WasmtimeException;
import wasmruntime.Types.FuncType;
import wasmruntime.Types.Value;

public class ModuleWrapper {
  public static final String compileTo = "DEBUG";
  public String moduleName;

  private long InstanceID;

  public Map<String, FuncType> exportedFunctions = new HashMap<String, FuncType>();

  public Map<String, WasmType> exportedGlobals = new HashMap<String, WasmType>();

  public static Map<String, Value<?>> knownSettings = new HashMap<String, Value<?>>();

  static {
    try {
      String fileName;

      if (compileTo == "DEBUG") {
        fileName = "debug/Wasmtime_embedding.dll";
      } else if (compileTo == "WINDOWS") {
        fileName = "x86_64-pc-windows-gnu/release/Wasmtime_embedding.dll";
      } else if (compileTo == "LINUX") {
        fileName = "x86_64-unknown-linux-gnu/release/Wasmtime_embedding.so";
      }

      URL res = ModuleWrapper.class.getClassLoader().getResource("Wasmtime-embedding/target/" + fileName);
      File file = Paths.get(res.toURI()).toFile();
      System.load(file.getAbsolutePath());
      Init();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    } catch (WasmtimeException e) {
      throw new RuntimeException(e);
    }

    knownSettings.put("autoReload", Value.fromI32(0));
  }

  public ModuleWrapper(File file, String name) throws IOException, WasmtimeException {
    moduleName = name;
    InstanceID = LoadModule(file.getAbsolutePath());
    
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
  
  public void close() {
    UnloadModule(InstanceID);
  }

  private static native long LoadModule(String path) throws WasmtimeException;

  private static native void Init() throws WasmtimeException;

  private static native void UnloadModule(long InstancePtr);

  private static native Map<String, List<Byte>> Functions(long InstancePtr) throws WasmtimeException;

  private static native List<Value<?>> CallFunction(long InstancePtr, String name, List<Value<?>> params) throws WasmtimeException;

  private static native Map<String, Byte> Globals(long InstancePtr) throws WasmtimeException;

  private static native Value<?> GetGlobal(long InstancePtr, String name) throws WasmtimeException;
}
