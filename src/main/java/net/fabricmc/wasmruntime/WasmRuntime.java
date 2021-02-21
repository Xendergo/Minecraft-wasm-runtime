package net.fabricmc.wasmruntime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.CallbackI.P;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.lib.gson.JsonReader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

public class WasmRuntime implements ModInitializer {
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ServerLifecycleEvents.SERVER_STARTED.register((MinecraftServer server) -> {
			File file = new File(server.getSavePath(WorldSavePath.ROOT).toFile(), "wasm.json");

			if (file.exists()) {
				try {
					FileReader reader = new FileReader(file);

					JsonReader jsonReader = new JsonReader(reader);
					List<String> modulesPaths = new ArrayList<String>();

					try {
						jsonReader.beginArray();
						while (jsonReader.hasNext()) {
							modulesPaths.add(jsonReader.nextString());
						}
						jsonReader.endArray();
	
						jsonReader.close();
					} catch (Exception e) {
						System.out.println("Error reading wasm.json file");
					}

					File configFolder = new File(server.getRunDirectory(), "config/wasm");

					for (String modulePath : modulesPaths) {
						try {
							Modules.LoadModule(new File(configFolder, modulePath));
						} catch (IOException e) {
							System.out.printf("Error reading file %s", modulePath);
							System.out.println(e);
						}
					}

				} catch (FileNotFoundException e) {
					System.out.println("Couldn't read wasm.json file");
				}
			} else {
				try {
					file.createNewFile();

					FileWriter writer = new FileWriter(file);
					writer.write("[\n  \n]");
					writer.close();
				} catch (IOException e) {
					System.out.println("Couldn't create wasm.json file");
				}
			}
		});
	}
}
