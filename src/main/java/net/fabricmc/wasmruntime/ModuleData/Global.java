package net.fabricmc.wasmruntime.ModuleData;

import net.fabricmc.wasmruntime.ModuleExecutor.Value;

public class Global<T extends Value> {
  public T value;
  public boolean mutable;

  public Global(T valueOof, boolean mutableOof) {
    value = valueOof;
    mutable = mutableOof;
  }

  public String toString() {
    return "Global: " + value + (mutable ? ", mutable" : "");
  }
}
