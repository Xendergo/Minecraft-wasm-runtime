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
import net.fabricmc.loader.lib.gson.JsonReader;
import wasmruntime.Commands.Invoke;
import wasmruntime.Commands.Load;
import wasmruntime.Commands.Suggestions.ExportedFunctions;
import wasmruntime.Commands.Suggestions.LoadableModules;
import wasmruntime.Commands.Suggestions.LoadedModules;
import wasmruntime.Errors.WasmParseError;
import wasmruntime.Errors.WasmValidationError;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import static net.minecraft.server.command.CommandManager.*;
import static com.mojang.brigadier.arguments.StringArgumentType.*;

public class WasmRuntime implements ModInitializer {
	public static File configFolder;
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		
		ServerLifecycleEvents.SERVER_STARTED.register((MinecraftServer server) -> {
			configFolder = new File(server.getRunDirectory(), "config/wasm");

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

					for (String modulePath : modulesPaths) {
						try {
							File module = new File(configFolder, modulePath);
							Modules.LoadModule(module, module.getName());
						} catch (IOException e) {
							System.out.printf("Error reading file %s\n", modulePath);
						} catch (WasmParseError e) {
							System.out.printf("Error parsing file %s: %s\n", modulePath, e.toString());
						} catch (WasmValidationError e) {
							System.out.printf("Error validating file %s: %s\n", modulePath, e.toString());
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
	}
}
