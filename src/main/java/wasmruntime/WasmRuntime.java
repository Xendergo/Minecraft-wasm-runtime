package wasmruntime;

import java.io.File;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraft.server.command.CommandManager.*;
import static com.mojang.brigadier.arguments.StringArgumentType.*;

public class WasmRuntime implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger();

	public static final File configFolder = new File("config/wasm");
	static AutoReloader reloadThread;

	static {
		reloadThread = new AutoReloader();
		reloadThread.start();
	}

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStart);
		ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStop);

		if (FabricLoader.getInstance().isModLoaded("carpet")) {
			Extension.LoadExtension();
		}

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

	private void onServerStart(MinecraftServer server) {
		reloadThread.changeMinecraftServer(server);

		try {
			readWasmJSON(server);
		} catch (IOException e) {
			LOGGER.info("Couldn't read wasm.json:");
			e.printStackTrace();
		}
	}

	private void onServerStop(MinecraftServer server) {
		reloadThread.changeMinecraftServer(null);
		for (String key : Modules.allModules()) {
			Modules.UnloadModule(key);
		}
	}

	private void readWasmJSON(MinecraftServer server) throws IOException {
		File file = new File(server.getSavePath(WorldSavePath.ROOT).toFile(), "wasm.json");

		if (file.exists()) {
			FileReader reader = new FileReader(file);

			try (JsonReader jsonReader = new JsonReader(reader);) {
				List<String> modulesPaths = new ArrayList<>();

				jsonReader.beginArray();
				while (jsonReader.hasNext()) {
					modulesPaths.add(jsonReader.nextString());
				}
				jsonReader.endArray();

				for (String modulePath : modulesPaths) {
					try {
						Modules.LoadModule(server, modulePath);
					} catch (WasmtimeException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			file.createNewFile();

			try (FileWriter writer = new FileWriter(file);) {
				writer.write("[\n  \n]");
			}
		}
	}
}
