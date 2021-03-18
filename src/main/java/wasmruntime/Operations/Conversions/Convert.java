package wasmruntime.Operations.Conversions;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

public class Convert {
  public static void f32_i32_s(ValueStack stack) {
    stack.push((float) i32(stack));
  }

  public static void f32_i32_u(ValueStack stack) {
    stack.push(UnsignedInteger.fromIntBits(i32(stack)).floatValue());
  }

  public static void f64_i32_s(ValueStack stack) {
    stack.push((float) i32(stack));
  }

  public static void f64_i32_u(ValueStack stack) {
    stack.push(UnsignedInteger.fromIntBits(i32(stack)).doubleValue());
  }

  public static void f32_i64_s(ValueStack stack) {
    stack.push((float) i64(stack));
  }

  public static void f32_i64_u(ValueStack stack) {
    stack.push(UnsignedLong.valueOf(i64(stack)).floatValue());
  }

  public static void f64_i64_s(ValueStack stack) {
    stack.push((float) i64(stack));
  }

  public static void f64_i64_u(ValueStack stack) {
    stack.push(UnsignedLong.valueOf(i64(stack)).doubleValue());
  }
}
