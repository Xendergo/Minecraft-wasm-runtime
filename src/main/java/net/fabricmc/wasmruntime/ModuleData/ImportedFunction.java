package net.fabricmc.wasmruntime.ModuleData;

import net.fabricmc.wasmruntime.ModuleExecutor.ValueStack;

public class ImportedFunction extends WasmFunctionInterface {
  String module;
  String name;

	public ImportedFunction(String moduleOof, String nameOof, FunctionType typeOof) {
    module = moduleOof;
    name = nameOof;
    type = typeOof;
	}

  @Override
  public String toString() {
    return "ImportedFunction {module: "+module+", name: "+name+"}";
  }

  /*
  TODO: This
  */
  @Override
  public ValueStack Exec() {
    return new ValueStack(0);
  }
}
