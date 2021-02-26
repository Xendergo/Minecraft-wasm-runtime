package net.fabricmc.wasmruntime.ModuleData;

import net.fabricmc.wasmruntime.ModuleExecutor.ValueStack;

public abstract class WasmFunctionInterface {
  public FunctionType type;
  public abstract ValueStack Exec();
}
