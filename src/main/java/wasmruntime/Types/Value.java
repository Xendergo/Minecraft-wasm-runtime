package wasmruntime.Types;

import wasmruntime.Enums.WasmType;

public class Value<T extends Number> {
  public T value;
  public WasmType type;

  private Value(T value, WasmType type) {
    this.value = value;
    this.type = type;
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
    return new Value<Integer>(v, WasmType.I32);
  }

  public static Value<Long> fromI64(long v) {
    return new Value<Long>(v, WasmType.I64);
  }

  public static Value<Float> fromF32(float v) {
    return new Value<Float>(v, WasmType.F32);
  }

  public static Value<Double> fromF64(double v) {
    return new Value<Double>(v, WasmType.F64);
  }

  public int getType() {
    return type.getNum();
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
