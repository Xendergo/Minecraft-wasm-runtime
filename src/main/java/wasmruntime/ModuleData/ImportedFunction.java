package wasmruntime.ModuleData;

import wasmruntime.ModuleExecutor.ValueStack;

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
  public void Exec(ValueStack stack) {
    
  }
}
