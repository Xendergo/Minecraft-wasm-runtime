package wasmruntime.Types;

public class Value<T extends Number> {
  public T value;

  private Value(T value) {
    this.value = value;
  }

  public int i32() {
    return (int) value;
  }

  public long i64() {
    return (long) value;
  }

  public float f32() {
    return (float) value;
  }

  public double f64() {
    return (double) value;
  }
  
  public static Value<Integer> fromI32(int v) {
    return new Value<Integer>(v);
  }

  public static Value<Long> fromI64(long v) {
    return new Value<Long>(v);
  }

  public static Value<Float> fromF32(float v) {
    return new Value<Float>(v);
  }

  public static Value<Double> fromF64(double v) {
    return new Value<Double>(v);
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
