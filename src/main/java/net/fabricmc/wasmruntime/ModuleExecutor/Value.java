package net.fabricmc.wasmruntime.ModuleExecutor;

import net.fabricmc.wasmruntime.ModuleData.HelpfulEnums.WasmType;

public interface Value {
  public WasmType type = WasmType.i32;

  public String toString();
}
