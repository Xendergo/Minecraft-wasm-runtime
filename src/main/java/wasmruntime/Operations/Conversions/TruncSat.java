package wasmruntime.Operations.Conversions;

import wasmruntime.ModuleExecutor.ValueStack;
import wasmruntime.Operations.Sat;

public class TruncSat {
  public static void f32_i32_s(ValueStack stack) {
    Sat.f32_i32_s(stack);
    Trunc.f32_i32_s(stack);
  }

  public static void f32_i32_u(ValueStack stack) {
    Sat.f32_i32_u(stack);
    Trunc.f32_i32_u(stack);
  }

  public static void f64_i32_s(ValueStack stack) {
    Sat.f64_i32_s(stack);
    Trunc.f64_i32_s(stack);
  }

  public static void f64_i32_u(ValueStack stack) {
    Sat.f64_i32_u(stack);
    Trunc.f64_i32_u(stack);
  }

  public static void f32_i64_s(ValueStack stack) {
    Sat.f32_i64_s(stack);
    Trunc.f32_i64_s(stack);
  }

  public static void f32_i64_u(ValueStack stack) {
    Sat.f32_i64_u(stack);
    Trunc.f32_i64_u(stack);
  }

  public static void f64_i64_s(ValueStack stack) {
    Sat.f64_i64_s(stack);
    Trunc.f64_i64_s(stack);
  }

  public static void f64_i64_u(ValueStack stack) {
    Sat.f64_i64_u(stack);
    Trunc.f64_i64_u(stack);
  }
}
