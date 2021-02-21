package net.fabricmc.wasmruntime.ModuleData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Module {
  public static int WasmVersion = 1;

  public HashMap<String, byte[]> CustomSection = new HashMap<String, byte[]>();
  public List<FunctionType> TypeSection = new ArrayList<FunctionType>();
}
