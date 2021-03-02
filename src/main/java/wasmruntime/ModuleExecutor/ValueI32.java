package wasmruntime.ModuleExecutor;

public class ValueI32 implements Value {
  public int value = 0;

  public ValueI32(int val) {
    value = val;
  }

  public String toString() {
    return "Value: " + value;
  }

  public static ValueI32 fromInt(int i) {
    return new ValueI32(i);
  }
}
