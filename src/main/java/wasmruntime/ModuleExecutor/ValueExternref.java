package wasmruntime.ModuleExecutor;

public class ValueExternref implements Value {
  public int value = 0;

  public ValueExternref(int val) {
    value = val;
  }

  public String toString() {
    return "externref: " + value;
  }
}
