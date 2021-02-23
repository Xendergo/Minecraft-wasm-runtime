package net.fabricmc.wasmruntime.ModuleData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Module {
  public static int WasmVersion = 1;

  public HashMap<String, byte[]> CustomSection = new HashMap<String, byte[]>();
  public List<FunctionType> TypeSection = new ArrayList<FunctionType>();
  public List<WasmFunction> Functions = new ArrayList<WasmFunction>();
  public List<WasmTable> Tables = new ArrayList<WasmTable>();
  public List<Memory> Memories = new ArrayList<Memory>();
  public List<Global<?>> Globals = new ArrayList<Global<?>>();

  public HashMap<String, Export> exportedFunctions = new HashMap<String, Export>();
  public HashMap<String, Export> exportedGlobals = new HashMap<String, Export>();
  public Export exportedMemory;

}
