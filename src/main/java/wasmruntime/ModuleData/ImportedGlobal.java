package wasmruntime.ModuleData;

import wasmruntime.ModuleData.HelpfulEnums.WasmType;
import wasmruntime.ModuleExecutor.Value;

public class ImportedGlobal extends Global {
  public String module;
  public String name;

	public ImportedGlobal(String moduleOof, String nameOof, boolean mutableOof, WasmType type) {
		super(null, mutableOof, type);

    module = moduleOof;
    name = nameOof;
	}

  // TODO: this
  @Override
  public void setValue(Value newValue) {
    super.setValue(newValue);
  }

  // TODO: this
  @Override
  public Value getValue() {
    return super.getValue();
  }

  @Override
  public String toString() {
    return "ImportedGlobal {module: " + module + ", name: " + name + ", value: " + getValue() + "}";
  }
}
