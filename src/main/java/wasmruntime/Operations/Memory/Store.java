package wasmruntime.Operations.Memory;

import wasmruntime.ModuleExecutor.ValueF32;
import wasmruntime.ModuleExecutor.ValueF64;
import wasmruntime.ModuleExecutor.ValueI32;
import wasmruntime.ModuleExecutor.ValueI64;
import wasmruntime.ModuleExecutor.ValueFuncref;
import wasmruntime.Errors.TrapRuntime;
import wasmruntime.ModuleExecutor.Value;
import wasmruntime.ModuleExecutor.ValueExternref;
import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

import java.nio.ByteBuffer;

public class Store {
  public static void storeI32(ValueStack stack) {
    int pos = i32(stack) + i32(immediates[1]);
    ByteBuffer buf = stack.module.Memories.get(0).data;
    if (pos + 4 > buf.capacity()) throw new TrapRuntime("Can't store i32 outside the capacity of memory");

    buf.putInt(pos, i32(stack));
  }

  public static void storeI64(ValueStack stack) {
    int pos = i32(stack) + i32(immediates[1]);
    ByteBuffer buf = stack.module.Memories.get(0).data;
    if (pos + 8 > buf.capacity()) throw new TrapRuntime("Can't store i64 outside the capacity of memory");

    buf.putLong(pos, i64(stack));
  }

  public static void storeF32(ValueStack stack) {
    int pos = i32(stack) + i32(immediates[1]);
    ByteBuffer buf = stack.module.Memories.get(0).data;
    if (pos + 4 > buf.capacity()) throw new TrapRuntime("Can't store f32 outside the capacity of memory");

    buf.putFloat(pos, f32(stack));
  }

  public static void storeF64(ValueStack stack) {
    int pos = i32(stack) + i32(immediates[1]);
    ByteBuffer buf = stack.module.Memories.get(0).data;
    if (pos + 8 > buf.capacity()) throw new TrapRuntime("Can't store f64 outside the capacity of memory");

    buf.putDouble(pos, f64(stack));
  }

  public static void storeI32_8(ValueStack stack) {
    int pos = i32(stack) + i32(immediates[1]);
    ByteBuffer buf = stack.module.Memories.get(0).data;
    if (pos + 1 > buf.capacity()) throw new TrapRuntime("Can't store byte outside the capacity of memory");

    buf.put(pos, (byte) i32(stack));
  }

  public static void storeI32_16(ValueStack stack) {
    int pos = i32(stack) + i32(immediates[1]);
    ByteBuffer buf = stack.module.Memories.get(0).data;
    if (pos + 2 > buf.capacity()) throw new TrapRuntime("Can't store short outside the capacity of memory");

    buf.putShort(pos, (short) i32(stack));
  }

  public static void storeI64_8(ValueStack stack) {
    int pos = i32(stack) + i32(immediates[1]);
    ByteBuffer buf = stack.module.Memories.get(0).data;
    if (pos + 1 > buf.capacity()) throw new TrapRuntime("Can't store byte outside the capacity of memory");

    buf.put(pos, (byte) i64(stack));
  }

  public static void storeI64_16(ValueStack stack) {
    int pos = i32(stack) + i32(immediates[1]);
    ByteBuffer buf = stack.module.Memories.get(0).data;
    if (pos + 2 > buf.capacity()) throw new TrapRuntime("Can't store short outside the capacity of memory");

    buf.putShort(pos, (short) i64(stack));
  }

  public static void storeI64_32(ValueStack stack) {
    int pos = i32(stack) + i32(immediates[1]);
    ByteBuffer buf = stack.module.Memories.get(0).data;
    if (pos + 4 > buf.capacity()) throw new TrapRuntime("Can't store short outside the capacity of memory");

    buf.putInt(pos, (int) i32(stack));
  }
}
