package wasmruntime.ModuleExecutor;

public class ValueI64 implements Value {
  public Long value = 0L;

  public ValueI64(Long val) {
    value = val;
  }

  public String toString() {
    return "i64: " + value;
  }
}
