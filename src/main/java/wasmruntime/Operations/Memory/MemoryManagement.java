package wasmruntime.Operations.Memory;

import wasmruntime.ModuleData.Memory;
import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MemoryManagement {
  public static void size(ValueStack stack) {
    stack.push(stack.module.Memories.get(i32(immediates[0])).data.capacity() >> 16);
  }

  public static void grow(ValueStack stack) {
    Memory memory = stack.module.Memories.get(i32(immediates[0]));

    int newSize = i32(stack) + (memory.data.capacity() >> 16);

    if (memory.limit.withinLimits(newSize)) {
      stack.push(memory.data.capacity() >> 16);

      ByteBuffer old = memory.data;
      memory.data = ByteBuffer.allocate(newSize << 16);
      memory.data.order(ByteOrder.LITTLE_ENDIAN);

      old.position(0);
      memory.data.position(0);
      memory.data.put(old);
    } else {
      stack.push(-1);
    }
  }
}
