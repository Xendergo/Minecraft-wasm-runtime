package wasmruntime.ModuleExecutor;

public class ValueFuncref implements Value {
  public long value = 0;

  public ValueFuncref(long val) {
    value = val;
  }

  public String toString() {
    return "funcref: " + value;
  }
}
