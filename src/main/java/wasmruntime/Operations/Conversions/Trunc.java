package wasmruntime.Operations.Conversions;

import wasmruntime.ModuleExecutor.ValueStack;
import wasmruntime.Operations.Math.FloatingPoint.Rounding;

import static wasmruntime.ModuleData.Opcodes.*;

import com.google.common.primitives.UnsignedInts;
import com.google.common.primitives.UnsignedLong;
import com.ibm.icu.math.BigDecimal;

public class Trunc {
  public static void f32_i32_s(ValueStack stack) {
    Rounding.trunc_f32(stack);
    stack.push((int) f32(stack));
  }

  public static void f32_i32_u(ValueStack stack) {
    Rounding.trunc_f32(stack);
    stack.push(UnsignedInts.saturatedCast((long) f32(stack)));
  }

  public static void f64_i32_s(ValueStack stack) {
    Rounding.trunc_f64(stack);
    stack.push((int) f64(stack));
  }

  public static void f64_i32_u(ValueStack stack) {
    Rounding.trunc_f64(stack);
    stack.push(UnsignedInts.saturatedCast((long) f64(stack)));
  }

  public static void f32_i64_s(ValueStack stack) {
    Rounding.trunc_f32(stack);
    stack.push((long) f32(stack));
  }

  public static void f32_i64_u(ValueStack stack) {
    Rounding.trunc_f32(stack);
    try {
      stack.push(UnsignedLong.valueOf(new BigDecimal(f32(stack)).toBigInteger()).longValue());
    } catch (Exception e) {
      stack.push(0L);
    }
  }

  public static void f64_i64_s(ValueStack stack) {
    Rounding.trunc_f64(stack);
    stack.push((long) f64(stack));
  }

  public static void f64_i64_u(ValueStack stack) {
    Rounding.trunc_f64(stack);
    try {
      stack.push(UnsignedLong.valueOf(new BigDecimal(f64(stack)).toBigInteger()).longValue());
    } catch (Exception e) {
      stack.push(0L);
    }
  }
}
