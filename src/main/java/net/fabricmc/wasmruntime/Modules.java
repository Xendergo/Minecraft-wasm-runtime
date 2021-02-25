package net.fabricmc.wasmruntime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.fabricmc.wasmruntime.Errors.WasmParseError;
import net.fabricmc.wasmruntime.ModuleParsing.Parser;
import net.fabricmc.wasmruntime.ModuleData.Module;

public class Modules {
  public static Map<String, Module> modules = new HashMap<String, Module>();
  public static void LoadModule(File path, String name) throws IOException, WasmParseError {
    FileInputStream stream = new FileInputStream(path);

    byte[] bytes = stream.readAllBytes();

    stream.close();

    modules.put(name, Parser.parseModule(bytes));

    System.out.println(modules.get(name));
  }
}