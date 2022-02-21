package wasmruntime.Types;

import carpet.script.exception.InternalExpressionException;

public class Value<T> {
    public T value;
    InteroperableType type;

    Value(T value, InteroperableType type) {
        this.value = value;
        this.type = type;
    }

    public static Value<?> parse(String value, InteroperableType type) {
        switch (type) {
            case Bool: {
                return new Value<>(Boolean.parseBoolean(value), type);
            }

            case U8: {
                var parsed = Long.parseUnsignedLong(value);
                if (parsed >= 1 << 8) {
                    throw new NumberFormatException("U8 is out of range");
                }
                return new Value<>((byte) parsed, type);
            }

            case I8: {
                return new Value<>(Byte.parseByte(value), type);
            }

            case U16: {
                var parsed = Long.parseUnsignedLong(value);
                if (parsed >= 1 << 16) {
                    throw new NumberFormatException("U16 is out of range");
                }
                return new Value<>((short) parsed, type);
            }

            case I16: {
                return new Value<>(Short.parseShort(value), type);
            }

            case U32: {
                var parsed = Long.parseUnsignedLong(value);
                if (parsed >= ((long) 1) << 32) {
                    throw new NumberFormatException("U32 is out of range");
                }
                return new Value<>((int) parsed, type);
            }

            case I32: {
                return new Value<>(Integer.parseInt(value), type);
            }

            case U64: {
                return new Value<>(Long.parseUnsignedLong(value), type);
            }

            case I64: {
                return new Value<>(Long.parseLong(value), type);
            }

            case F32: {
                return new Value<>(Float.parseFloat(value), type);
            }

            case F64: {
                return new Value<>(Double.parseDouble(value), type);
            }

            case String: {
                return new Value<>(value, type);
            }

            default: {
                throw new RuntimeException("Unreachable");
            }
        }
    }

    public static Value<?> parseScarpet(carpet.script.value.Value value, InteroperableType type) {
        switch (type) {
            case Bool: {
                return new Value<>(value.getBoolean(), type);
            }

            case U8: {
                var v = value.readInteger();
                if (v < 0 || v >= 1 << 8) {
                    throw new InternalExpressionException("U8 is out of range");
                }
                return new Value<>((byte) v, type);
            }

            case I8: {
                var v = value.readInteger();
                if (v < -(1 << 7) || v >= 1 << 7) {
                    throw new InternalExpressionException("I8 is out of range");
                }
                return new Value<>((byte) v, type);
            }

            case U16: {
                var v = value.readInteger();
                if (v < 0 || v >= 1 << 16) {
                    throw new InternalExpressionException("U16 is out of range");
                }
                return new Value<>((short) v, type);
            }

            case I16: {
                var v = value.readInteger();
                if (v < -(1 << 15) || v >= 1 << 15) {
                    throw new InternalExpressionException("I16 is out of range");
                }
                return new Value<>((short) v, type);
            }

            case U32: {
                var v = value.readInteger();
                if (v < 0 || v >= ((long) 1) << 32) {
                    throw new InternalExpressionException("U32 is out of range");
                }
                return new Value<>((int) v, type);
            }

            case I32: {
                var v = value.readInteger();
                if (v < -(1 << 31) || v >= 1 << 31) {
                    throw new InternalExpressionException("I32 is out of range");
                }
                return new Value<>((int) v, type);
            }

            case U64: {
                var v = value.readInteger();
                if (v < 0) {
                    throw new InternalExpressionException("U64 is out of range");
                }
                return new Value<>(v, type);
            }

            case I64: {
                return new Value<>(value.readInteger(), type);
            }

            case F32: {
                return new Value<>((float) value.readDoubleNumber(), type);
            }

            case F64: {
                return new Value<>(value.readDoubleNumber(), type);
            }

            case String: {
                return new Value<>(value.getString(), type);
            }

            default: {
                throw new RuntimeException("Unreachable");
            }
        }
    }

    public carpet.script.value.Value intoScarpet() {
        switch (this.type) {
            case Bool:
                return carpet.script.value.BooleanValue.of((Boolean) this.value);
            case U8, I8, U16, I16, U32, I32, U64, I64:
                return carpet.script.value.NumericValue.of((Long) this.value);
            case F32, F64:
                return carpet.script.value.NumericValue.of((Double) this.value);
            case String:
                return carpet.script.value.StringValue.of((String) this.value);
            default:
                throw new RuntimeException("Unreachable");
        }
    }

    public InteroperableType type() {
        return type;
    }

    public int typeNum() {
        return type.getNum();
    }

    public boolean bool() {
        return (boolean) value;
    }

    public byte i8() {
        return (byte) value;
    }

    public short i16() {
        return (short) value;
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

    public String string() {
        return (String) value;
    }

    public static Value<Integer> fromI32(int v) {
        return new Value<>(v, InteroperableType.I32);
    }

    public static Value<Long> fromI64(long v) {
        return new Value<>(v, InteroperableType.I64);
    }

    public static Value<Float> fromF32(float v) {
        return new Value<>(v, InteroperableType.F32);
    }

    public static Value<Double> fromF64(double v) {
        return new Value<>(v, InteroperableType.F64);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
