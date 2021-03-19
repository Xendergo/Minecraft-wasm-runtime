package wasmruntime.Operations;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

public class Sat {
  public static void f32_i32_s(ValueStack stack) {
    stack.push(Math.min(Math.max(Integer.MIN_VALUE, f32(stack)), Integer.MAX_VALUE));
  }

  public static void f32_i32_u(ValueStack stack) {
    stack.push(Math.min(Math.max(0, f32(stack)), UnsignedInteger.MAX_VALUE.floatValue()));
  }

  public static void f32_i64_s(ValueStack stack) {
    stack.push(Math.min(Math.max(Long.MIN_VALUE, f32(stack)), Long.MAX_VALUE));
  }

  public static void f32_i64_u(ValueStack stack) {
    stack.push(Math.min(Math.max(0, f32(stack)), UnsignedLong.MAX_VALUE.intValue()));
  }

  public static void f64_i32_s(ValueStack stack) {
    stack.push(Math.min(Math.max(Integer.MIN_VALUE, f64(stack)), Integer.MAX_VALUE));
  }

  public static void f64_i32_u(ValueStack stack) {
    stack.push(Math.min(Math.max(0, f64(stack)), UnsignedInteger.MAX_VALUE.doubleValue()));
  }

  public static void f64_i64_s(ValueStack stack) {
    stack.push(Math.min(Math.max(Long.MIN_VALUE, f64(stack)), Long.MAX_VALUE));
  }

  public static void f64_i64_u(ValueStack stack) {
    stack.push(Math.min(Math.max(0, f64(stack)), UnsignedLong.MAX_VALUE.doubleValue()));
  }
}
