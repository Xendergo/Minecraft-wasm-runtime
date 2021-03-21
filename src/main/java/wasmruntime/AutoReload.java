package wasmruntime;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;

public class AutoReload implements FileListener {

  // It doesn't matter if a file gets deleted
  public void fileDeleted(FileChangeEvent event) throws Exception {}

  public void fileCreated(FileChangeEvent event) throws Exception {
    tryReload(event);
  }

  public void fileChanged(FileChangeEvent event) throws Exception {
    tryReload(event);
  }

  private void tryReload(FileChangeEvent event) throws IOException {
    String moduleName = FilenameUtils.getBaseName(event.getFileObject().getName().getBaseName());
    
    System.out.println("Yee");

    if (!Modules.modules.containsKey(moduleName)) return;
    ModuleWrapper module = Modules.modules.get(moduleName);

    // if (module.getFunc("autoReload").call()[0].i32() == 1) {
    //   Modules.LoadModule(event.getFileObject());
    // }
  }
}
