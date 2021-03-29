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

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import wasmruntime.Exceptions.WasmtimeException;

public class ModuleWrapper {
  public String moduleName;

  private long InstanceID;

  public Map<String, FuncType> exportedFunctions = new HashMap<String, FuncType>();

  static {
    try {
      URL res = ModuleWrapper.class.getClassLoader().getResource("Wasmtime-embedding/target/debug/Wasmtime_embedding.dll");
      File file = Paths.get(res.toURI()).toFile();
      System.load(file.getAbsolutePath());
      Init();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    } catch (WasmtimeException e) {
      throw new RuntimeException(e);
    }
  }

  public ModuleWrapper(FileObject file, String name) throws FileSystemException, IOException, WasmtimeException {
    moduleName = name;
    InstanceID = LoadModule(file.getPath().toAbsolutePath().toString());
    
    for (Entry<String,List<Byte>> entry : Functions(InstanceID).entrySet()) {
      exportedFunctions.put(entry.getKey(), new FuncType(entry.getValue()));
    }

    System.out.println(exportedFunctions);
  }
  
  public void close() {
    UnloadModule();
  }

  private static native long LoadModule(String path) throws WasmtimeException;

  private static native void Init() throws WasmtimeException;

  private static native void UnloadModule();

  private static native Map<String, List<Byte>> Functions(long InstancePtr) throws WasmtimeException;
}
