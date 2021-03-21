package wasmruntime;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

public class ModuleWrapper {
  public String moduleName;

  static {
    // System.out.println(System.getProperty("java.library.path"));
    try {
      URL res = ModuleWrapper.class.getClassLoader().getResource("Wasmtime-embedding/target/debug/Wasmtime_embedding.dll");
      File file = Paths.get(res.toURI()).toFile();
      System.load(file.getAbsolutePath());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public ModuleWrapper(FileObject file, String name) throws FileSystemException, IOException {
    moduleName = name;
    System.out.println(yee("yoy"));
  }

  private static native String yee(String str);
}
