package wasmruntime.Operations.Bitwise;

import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

public class Rotate {
  public static void rotl_i32(ValueStack stack) {
    int b = i32(stack);
    stack.push(Integer.rotateLeft(i32(stack), b));
  }

  public static void rotr_i32(ValueStack stack) {
    int b = i32(stack);
    stack.push(Integer.rotateRight(i32(stack), b));
  }

  public static void rotl_i64(ValueStack stack) {
    long b = i64(stack);
    stack.push(Long.rotateLeft(i64(stack), (int) b));
  }

  public static void rotr_i64(ValueStack stack) {
    long b = i64(stack);
    stack.push(Long.rotateRight(i64(stack), (int) b));
  }
}
