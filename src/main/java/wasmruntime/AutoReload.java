package wasmruntime;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;

import wasmruntime.Errors.WasmParseError;
import wasmruntime.Errors.WasmValidationError;
import wasmruntime.ModuleData.Module;

public class AutoReload implements FileListener {

  // It doesn't matter if a file gets deleted
  public void fileDeleted(FileChangeEvent event) throws Exception {}

  public void fileCreated(FileChangeEvent event) throws Exception {
    tryReload(event);
  }

  public void fileChanged(FileChangeEvent event) throws Exception {
    tryReload(event);
  }

  private void tryReload(FileChangeEvent event) throws IOException, WasmParseError, WasmValidationError {
    String moduleName = FilenameUtils.getBaseName(event.getFileObject().getName().getBaseName());
    
    System.out.println("Yee");

    if (!Modules.modules.containsKey(moduleName)) return;
    Module module = Modules.modules.get(moduleName);

    if (module.getGlobalSetting("autoReload", 0).equals(1)) {
      Modules.LoadModule(event.getFileObject());
    }
  }
}
