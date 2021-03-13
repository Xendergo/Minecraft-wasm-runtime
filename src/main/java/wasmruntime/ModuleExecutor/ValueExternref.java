package wasmruntime.ModuleExecutor;

public class ValueExternref implements Value {
  public long value = 0;

  public ValueExternref(long val) {
    value = val;
  }

  public String toString() {
    return "externref: " + value;
  }
}
