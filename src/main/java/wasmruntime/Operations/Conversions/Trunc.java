package wasmruntime.Operations.Conversions;

import wasmruntime.ModuleExecutor.ValueStack;
import wasmruntime.Operations.Math.FloatingPoint.Rounding;

import static wasmruntime.ModuleData.Opcodes.*;

import com.google.common.primitives.UnsignedInts;
import com.google.common.primitives.UnsignedLong;
import com.ibm.icu.math.BigDecimal;

import wasmruntime.Errors.TrapRuntime;

public class Trunc {
  public static void f32_i32_s(ValueStack stack) {
    Rounding.trunc_f32(stack);
    float v = f32(stack);
    if (v < Integer.MIN_VALUE || v > Integer.MAX_VALUE) throw new TrapRuntime("Can't convert " + v + " to an integer");
    stack.push((int) v);
  }

  public static void f32_i32_u(ValueStack stack) {
    Rounding.trunc_f32(stack);
    float v = f32(stack);
    if (v < 0 || v > 4294967296L) throw new TrapRuntime("Can't convert " + v + " to an unsigned integer");
    stack.push(UnsignedInts.saturatedCast((long) v));
  }

  public static void f64_i32_s(ValueStack stack) {
    Rounding.trunc_f64(stack);
    double v = f64(stack);
    if (v < Integer.MIN_VALUE || v > Integer.MAX_VALUE) throw new TrapRuntime("Can't convert " + v + " to an integer");
    stack.push((int) v);
  }

  public static void f64_i32_u(ValueStack stack) {
    Rounding.trunc_f64(stack);
    double v = f64(stack);
    if (v < 0 || v > 4294967296L) throw new TrapRuntime("Can't convert " + v + " to an unsigned integer");
    stack.push(UnsignedInts.saturatedCast((long) v));
  }

  public static void f32_i64_s(ValueStack stack) {
    Rounding.trunc_f32(stack);
    float v = f32(stack);
    if (v < Long.MIN_VALUE || v > Long.MAX_VALUE) throw new TrapRuntime("Can't convert " + v + " to an integer");
    stack.push((long) v);
  }

  public static void f32_i64_u(ValueStack stack) {
    Rounding.trunc_f32(stack);
    float f = f32(stack);
    if (f < 0 || f > 18446744073709552000D) throw new TrapRuntime("Can't convert " + f + " to an unsigned integer");
    stack.push(UnsignedLong.valueOf(new BigDecimal(f).toBigInteger()).longValue());
  }

  public static void f64_i64_s(ValueStack stack) {
    Rounding.trunc_f64(stack);
    double v = f64(stack);
    if (v < Long.MIN_VALUE || v > Long.MAX_VALUE) throw new TrapRuntime("Can't convert " + v + " to an integer");
    stack.push((long) v);
  }

  public static void f64_i64_u(ValueStack stack) {
    Rounding.trunc_f64(stack);
    double f = f64(stack);
    if (f < 0 || f > 18446744073709552000D) throw new TrapRuntime("Can't convert " + f + " to an unsigned integer");
    stack.push(UnsignedLong.valueOf(new BigDecimal(f).toBigInteger()).longValue());
  }
}
