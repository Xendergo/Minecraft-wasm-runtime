package wasmruntime.ModuleExecutor;

public class ValueFuncref extends Value {
  public int value = 0;

  public ValueFuncref(int val) {
    value = val;
  }

  public String toString() {
    return "funcref: " + value;
  }

  public boolean equals(Value v) {
    if (!(v instanceof ValueFuncref)) return false;

    ValueFuncref v2 = (ValueFuncref)v;

    return value == v2.value;
  }
}
