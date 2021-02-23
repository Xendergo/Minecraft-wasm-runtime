package net.fabricmc.wasmruntime.ModuleData;

import net.fabricmc.wasmruntime.ModuleExecutor.Value;

public class Global<T extends Value> {
  private T value;
  public boolean mutable;

  public Global(T valueOof, boolean mutableOof) {
    value = valueOof;
    mutable = mutableOof;
  }

  public T getValue() {
    return value;
  }

  public String toString() {
    return "Global: " + value + (mutable ? ", mutable" : "");
  }
}
