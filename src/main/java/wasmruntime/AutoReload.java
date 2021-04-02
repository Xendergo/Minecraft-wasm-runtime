package wasmruntime;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

public class AutoReload extends Thread {
  public boolean getOofed = false;
  @Override
  public void run() {
    while (true) {
      if (getOofed) return;

      long currentTime = System.currentTimeMillis();

      for (File file : WasmRuntime.configFolder.listFiles()) {
        if (file.lastModified() - 1000 < currentTime) {
          String moduleName = FilenameUtils.getBaseName(file.getName());
            
          if (!Modules.modules.containsKey(moduleName)) continue;
          ModuleWrapper module = Modules.modules.get(moduleName);
      
          if (module.GetSetting("autoReload").i32() == 1) {
            try {
              Modules.LoadModule(file);
            } catch (Exception e) {
            }
          }
        }
      }

      try {
        sleep(1000);
      } catch (Exception e) {
        throw new RuntimeException("Autoreload thread was interrupted somehow");
      }
    }
  }
}
