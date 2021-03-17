package wasmruntime.ModuleExecutor;

import wasmruntime.ModuleData.HelpfulEnums.WasmType;

public abstract class Value {
  public WasmType type = WasmType.i32;

  public abstract String toString();

  public abstract boolean equals(Value v);

  public boolean equals(int v) {
    return equals(new ValueI32(v));
  }

  public boolean equals(long v) {
    return equals(new ValueI64(v));
  }

  public boolean equals(float v) {
    return equals(new ValueF32(v));
  }

  public boolean equals(double v) {
    return equals(new ValueF64(v));
  }
}
