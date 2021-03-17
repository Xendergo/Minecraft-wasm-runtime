package wasmruntime.ModuleExecutor;

public class ValueF64 extends Value {
  public Double value = 0D;

  public ValueF64(Double val) {
    value = val;
  }

  public String toString() {
    return "f64: " + value;
  }

  public boolean equals(Value v) {
    if (!(v instanceof ValueF64)) return false;

    ValueF64 v2 = (ValueF64)v;

    return value == v2.value;
  }
}
