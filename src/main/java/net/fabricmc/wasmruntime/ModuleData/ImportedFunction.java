package net.fabricmc.wasmruntime.ModuleData;

public class ImportedFunction extends WasmFunction {
  String module;
  String name;

	public ImportedFunction(String moduleOof, String nameOof, int typeIndexOof) {
		super(typeIndexOof);
    module = moduleOof;
    name = nameOof;
	}

  @Override
  public String toString() {
    return "ImportedFunction {module: "+module+", name: "+name+", typeIndex: "+typeIndex+"}";
  }
}
