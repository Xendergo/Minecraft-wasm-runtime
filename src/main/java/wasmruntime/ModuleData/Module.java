package wasmruntime.ModuleData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import wasmruntime.Errors.WasmValidationError;

public class Module {
  public static int WasmVersion = 1;

  public HashMap<String, byte[]> CustomSection = new HashMap<String, byte[]>();
  public List<FunctionType> TypeSection = new ArrayList<FunctionType>();
  public List<Integer> FunctionTypeIndices = new ArrayList<Integer>();
  public List<Table> Tables = new ArrayList<Table>();
  public List<Memory> Memories = new ArrayList<Memory>();
  public List<Global<?>> Globals = new ArrayList<Global<?>>();
  public List<Code> Codes = new ArrayList<Code>();

  public List<WasmFunctionInterface> Functions = new ArrayList<WasmFunctionInterface>();

  public HashMap<String, Export> exportedFunctions = new HashMap<String, Export>();
  public HashMap<String, Export> exportedGlobals = new HashMap<String, Export>();
  public Export exportedMemory;

  public int startFunction;

  public void IsValid() throws WasmValidationError {
    for (int i = 0; i < Tables.size(); i++) {
      if (!Tables.get(i).IsValid()) throw new WasmValidationError("Table index " + i + " is invalid");
    }

    for (int i = 0; i < Memories.size(); i++) {
      if (!Memories.get(i).IsValid()) throw new WasmValidationError("Memory index " + i + " is invalid");
    }

    for (int i = 0; i < Codes.size(); i++) {
      if (!Codes.get(i).IsValid(Globals.toArray(new Global[0]))) throw new WasmValidationError("Code index " + i + " is invalid");
    }
  }
}
