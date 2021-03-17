package wasmruntime.ModuleExecutor;

public class ValueI64 extends Value {
  public Long value = 0L;

  public ValueI64(Long val) {
    value = val;
  }

  public String toString() {
    return "i64: " + value;
  }

  public boolean equals(Value v) {
    if (!(v instanceof ValueI64)) return false;

    ValueI64 v2 = (ValueI64)v;

    return value == v2.value;
  }
}
