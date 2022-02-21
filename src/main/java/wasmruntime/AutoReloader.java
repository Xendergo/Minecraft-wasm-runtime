package wasmruntime;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import wasmruntime.Exceptions.WasmtimeEmbeddingException;
import wasmruntime.Utils.Message;

public class AutoReloader extends Thread {
    private MinecraftServer server = null;

    public void changeMinecraftServer(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        while (true) {
            if (server != null) {
                autoreload();
            }

            try {
                sleep(1000);
            } catch (InterruptedException e) {
                WasmRuntime.LOGGER.info("Autoreload thread was interrupted somehow");
                Thread.currentThread().interrupt();
            }
        }
    }

    private void autoreload() {
        long currentTime = System.currentTimeMillis();

        for (File file : WasmRuntime.configFolder.listFiles()) {
            if (file.lastModified() + 1000 > currentTime) {
                String moduleName = FilenameUtils.getBaseName(file.getName());

                if (!Modules.moduleExists(moduleName))
                    continue;
                ModuleWrapper module = Modules.loadedModules.get(moduleName);

                if (module.GetSetting("autoReload").i32() == 1) {
                    try {
                        Modules.LoadModule(server, file, moduleName);
                    } catch (WasmtimeEmbeddingException e) {
                        LiteralText text = new LiteralText(
                                "Couldn't reload module '" + moduleName + "': " + e.getMessage());
                        text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xff3838)));

                        Message.broadcast(server, text);
                    }
                }
            }
        }
    }
}
