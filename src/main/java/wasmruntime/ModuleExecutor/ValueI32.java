package wasmruntime.ModuleExecutor;

public class ValueI32 extends Value {
  public int value = 0;

  public ValueI32(int val) {
    value = val;
  }

  public String toString() {
    return "i32: " + value;
  }

  public static ValueI32 fromInt(int i) {
    return new ValueI32(i);
  }

  public boolean equals(Value v) {
    if (!(v instanceof ValueI32)) return false;

    ValueI32 v2 = (ValueI32)v;

    return value == v2.value;
  }
}
