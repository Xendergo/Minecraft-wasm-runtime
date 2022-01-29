package wasmruntime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.lib.gson.JsonReader;
import wasmruntime.CarpetStuff.Extension;
import wasmruntime.Commands.Invoke;
import wasmruntime.Commands.Load;
import wasmruntime.Commands.Suggestions.ExportedFunctions;
import wasmruntime.Commands.Suggestions.LoadableModules;
import wasmruntime.Commands.Suggestions.LoadedModules;
import wasmruntime.Enums.WasmType;
import wasmruntime.Exceptions.WasmtimeException;
import wasmruntime.Imports.Printing;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import static net.minecraft.server.command.CommandManager.*;
import static com.mojang.brigadier.arguments.StringArgumentType.*;

public class WasmRuntime implements ModInitializer {
	public static final File configFolder = new File("config/wasm");
	public static AutoReload reloadThread;
	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register((MinecraftServer server) -> {
			reloadThread = new AutoReload();
			reloadThread.start();

			File file = new File(server.getSavePath(WorldSavePath.ROOT).toFile(), "wasm.json");

			if (file.exists()) {
				try {
					FileReader reader = new FileReader(file);

					JsonReader jsonReader = new JsonReader(reader);
					List<String> modulesPaths = new ArrayList<>();

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

					for (String modulePath : modulesPaths) {
						try {
							Modules.LoadModule(modulePath);
						} catch (WasmtimeException e) {
							e.printStackTrace();
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
		
			Modules.server = server;
		});

		if (FabricLoader.getInstance().isModLoaded("carpet")) {
			Extension.LoadExtension();
		}

		ServerLifecycleEvents.SERVER_STOPPING.register((MinecraftServer server) -> {
			Modules.server = null;
			
			reloadThread.getOofed = true;
			for (String key : Modules.modules.keySet()) {
				Modules.UnloadModule(key);
			}
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register(literal("wasm")
																				.then(literal("invoke")
																					.then(argument("Module", string()).suggests(new LoadedModules())
																						.then(argument("Function", string()).suggests(new ExportedFunctions())
																							.executes(ctx -> Invoke.run(ctx, getString(ctx, "Module"), getString(ctx, "Function"), ""))
																							.then(argument("Arguments", greedyString())
																								.executes(ctx -> Invoke.run(ctx, getString(ctx, "Module"), getString(ctx, "Function"), getString(ctx, "Arguments")))
																							)
																						)
																					)
																				).then(literal("load")
																					.then(argument("Module", string()).suggests(new LoadableModules())
																						.executes(ctx -> Load.run(ctx, getString(ctx, "Module")))
																					)
																				).then(literal("reload")
																					.then(argument("Module", string()).suggests(new LoadedModules())
																						.executes(ctx -> Load.run(ctx, getString(ctx, "Module")))
																					)
																				)
																			);
		});


		// Register imports
		ModuleImports.Register("print", Printing::log);
		ModuleImports.Register("printString", Printing::logString, (types) -> {
			for (WasmType type : types) {
				if (type == WasmType.F32 || type == WasmType.F64) return false;
			}

			return true;
		});
	}
}
