package wasmruntime.ModuleData;

import wasmruntime.ModuleExecutor.Value;
import wasmruntime.ModuleExecutor.ValueStack;

public class ImportedFunction extends WasmFunctionInterface {
  String module;
  String name;
  int stackSize;

	public ImportedFunction(String moduleOof, String nameOof, FunctionType typeOof) {
    module = moduleOof;
    name = nameOof;
    type = typeOof;

    stackSize = Math.max(type.inputs.length, type.outputs.length);
	}

  @Override
  public String toString() {
    return "ImportedFunction {module: "+module+", name: "+name+"}";
  }

  /*
  TODO: This
  */
  @Override
  public ValueStack Exec(Value[] values, Module module) {
    return new ValueStack(0, module);
  }

  @Override
  public int getStackSize() {
    return stackSize;
  }
}
