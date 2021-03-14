package wasmruntime.Operations.Memory;

import wasmruntime.ModuleExecutor.ValueF32;
import wasmruntime.ModuleExecutor.ValueF64;
import wasmruntime.ModuleExecutor.ValueI32;
import wasmruntime.ModuleExecutor.ValueI64;
import wasmruntime.Errors.TrapRuntime;
import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

import java.nio.ByteBuffer;

public class Load {
  public static void loadI32(ValueStack stack) {
    int pos = i32(stack) + i32(immediates[1]);
    ByteBuffer buf = stack.module.Memories.get(0).data;
    if (pos + 4 > buf.capacity()) throw new TrapRuntime("Can't read i32 outside the capacity of memory");

    stack.push(new ValueI32(buf.getInt(pos)));
  }

  public static void loadI64(ValueStack stack) {
    int pos = i32(stack) + i32(immediates[1]);
    ByteBuffer buf = stack.module.Memories.get(0).data;
    if (pos + 8 > buf.capacity()) throw new TrapRuntime("Can't read i64 outside the capacity of memory");

    stack.push(new ValueI64(buf.getLong(pos)));
  }

  public static void loadF32(ValueStack stack) {
    int pos = i32(stack) + i32(immediates[1]);
    ByteBuffer buf = stack.module.Memories.get(0).data;
    if (pos + 4 > buf.capacity()) throw new TrapRuntime("Can't read f32 outside the capacity of memory");

    stack.push(new ValueF32(buf.getFloat(pos)));
  }

  public static void loadF64(ValueStack stack) {
    int pos = i32(stack) + i32(immediates[1]);
    ByteBuffer buf = stack.module.Memories.get(0).data;
    if (pos + 8 > buf.capacity()) throw new TrapRuntime("Can't read f64 outside the capacity of memory");

    stack.push(new ValueF64(buf.getDouble(pos)));
  }

  public static void loadI32_8u(ValueStack stack) {
    int pos = i32(stack) + i32(immediates[1]);
    ByteBuffer buf = stack.module.Memories.get(0).data;
    if (pos + 1 > buf.capacity()) throw new TrapRuntime("Can't read byte outside the capacity of memory");

    stack.push(new ValueI32(((int) buf.get(pos)) & 0xFF));
  }

  public static void loadI32_16u(ValueStack stack) {
    int pos = i32(stack) + i32(immediates[1]);
    ByteBuffer buf = stack.module.Memories.get(0).data;
    if (pos + 2 > buf.capacity()) throw new TrapRuntime("Can't read short outside the capacity of memory");

    stack.push(new ValueI32(((int) buf.getShort(pos)) & 0xFFFF));
  }

  public static void loadI64_8u(ValueStack stack) {
    int pos = i32(stack) + i32(immediates[1]);
    ByteBuffer buf = stack.module.Memories.get(0).data;
    if (pos + 1 > buf.capacity()) throw new TrapRuntime("Can't read byte outside the capacity of memory");

    stack.push(new ValueI64(((long) buf.get(pos)) & 0xFF));
  }

  public static void loadI64_16u(ValueStack stack) {
    int pos = i32(stack) + i32(immediates[1]);
    ByteBuffer buf = stack.module.Memories.get(0).data;
    if (pos + 2 > buf.capacity()) throw new TrapRuntime("Can't read short outside the capacity of memory");

    stack.push(new ValueI64(((long) buf.getShort(pos)) & 0xFFFF));
  }

  public static void loadI64_32u(ValueStack stack) {
    int pos = i32(stack) + i32(immediates[1]);
    ByteBuffer buf = stack.module.Memories.get(0).data;
    if (pos + 1 > buf.capacity()) throw new TrapRuntime("Can't read int outside the capacity of memory");

    stack.push(new ValueI64(((long) buf.getInt(pos)) & 0xFFFFFFFF));
  }

  public static void loadI32_8s(ValueStack stack) {
    int pos = i32(stack) + i32(immediates[1]);
    ByteBuffer buf = stack.module.Memories.get(0).data;
    if (pos + 1 > buf.capacity()) throw new TrapRuntime("Can't read byte outside the capacity of memory");

    stack.push(new ValueI32(buf.get(pos)));
  }

  public static void loadI32_16s(ValueStack stack) {
    int pos = i32(stack) + i32(immediates[1]);
    ByteBuffer buf = stack.module.Memories.get(0).data;
    if (pos + 2 > buf.capacity()) throw new TrapRuntime("Can't read short outside the capacity of memory");

    stack.push(new ValueI32(buf.getShort(pos)));
  }

  public static void loadI64_8s(ValueStack stack) {
    int pos = i32(stack) + i32(immediates[1]);
    ByteBuffer buf = stack.module.Memories.get(0).data;
    if (pos + 1 > buf.capacity()) throw new TrapRuntime("Can't read byte outside the capacity of memory");

    stack.push(new ValueI64((long) buf.get(pos)));
  }

  public static void loadI64_16s(ValueStack stack) {
    int pos = i32(stack) + i32(immediates[1]);
    ByteBuffer buf = stack.module.Memories.get(0).data;
    if (pos + 2 > buf.capacity()) throw new TrapRuntime("Can't read short outside the capacity of memory");

    stack.push(new ValueI64((long) buf.getShort(pos)));
  }

  public static void loadI64_32s(ValueStack stack) {
    int pos = i32(stack) + i32(immediates[1]);
    ByteBuffer buf = stack.module.Memories.get(0).data;
    if (pos + 1 > buf.capacity()) throw new TrapRuntime("Can't read int outside the capacity of memory");

    stack.push(new ValueI64((long) buf.getInt(pos)));
  }
}
