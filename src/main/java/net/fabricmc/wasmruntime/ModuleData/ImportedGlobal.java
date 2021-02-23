package net.fabricmc.wasmruntime.ModuleData;

import net.fabricmc.wasmruntime.ModuleExecutor.Value;

public class ImportedGlobal<T extends Value> extends Global<T> {
  public String module;
  public String name;

	public ImportedGlobal(String moduleOof, String nameOof, boolean mutableOof) {
		super(null, mutableOof);

    module = moduleOof;
    name = nameOof;
	}

  // Ima do this later
  @Override
  public T getValue() {
    return super.getValue();
  }

  @Override
  public String toString() {
    return "ImportedGlobal {module: " + module + ", name: " + name + ", value: " + getValue() + "}";
  }
}
