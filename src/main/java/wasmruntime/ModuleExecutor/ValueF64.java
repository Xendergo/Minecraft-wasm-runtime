package wasmruntime.ModuleExecutor;

public class ValueF64 implements Value {
  public Double value = 0D;

  public ValueF64(Double val) {
    value = val;
  }

  public String toString() {
    return "Value: " + value;
  }
}
