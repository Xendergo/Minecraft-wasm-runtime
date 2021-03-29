package wasmruntime;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import wasmruntime.Exceptions.WasmtimeException;

public class ModuleWrapper implements AutoCloseable {
  public String moduleName;

  private long InstanceID;

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
    System.out.println(Functions(InstanceID));
  }
  
  public void close() throws Exception {
    UnloadModule();
  }

  private static native long LoadModule(String path) throws WasmtimeException;

  private static native void Init() throws WasmtimeException;

  private static native void UnloadModule() throws WasmtimeException;

  private static native Map<String, List<Byte>> Functions(long InstancePtr) throws WasmtimeException;
}
