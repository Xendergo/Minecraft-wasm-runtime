package wasmruntime.ModuleExecutor;

public class ValueF32 extends Value {
  public float value = 0;

  public ValueF32(float val) {
    value = val;
  }

  public String toString() {
    return "f32: " + value;
  }

  public boolean equals(Value v) {
    if (!(v instanceof ValueF32)) return false;

    ValueF32 v2 = (ValueF32)v;

    return value == v2.value;
  }
}
