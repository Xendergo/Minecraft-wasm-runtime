package wasmruntime.ModuleExecutor;

public class ValueExternref extends Value {
  public int value = 0;

  public ValueExternref(int val) {
    value = val;
  }

  public String toString() {
    return "externref: " + value;
  }

  public boolean equals(Value v) {
    if (!(v instanceof ValueExternref)) return false;

    ValueExternref v2 = (ValueExternref)v;

    return value == v2.value;
  }
}
