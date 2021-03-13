package wasmruntime.ModuleExecutor;

public class ValueFuncref implements Value {
  public int value = 0;

  public ValueFuncref(int val) {
    value = val;
  }

  public String toString() {
    return "funcref: " + value;
  }
}
