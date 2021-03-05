package wasmruntime.ModuleExecutor;

public class ValueF32 implements Value {
  public float value = 0;

  public ValueF32(float val) {
    value = val;
  }

  public String toString() {
    return "f32: " + value;
  }
}
