package wasmruntime.Imports;

import java.util.ArrayList;
import java.util.List;

import wasmruntime.Enums.WasmType;
import wasmruntime.Exceptions.WasmtimeException;
import wasmruntime.Types.Value;
import wasmruntime.Utils.ImportCallCtx;
import wasmruntime.Utils.Message;

public class Printing {
  public static Value<?>[] log(ImportCallCtx ctx) {
    Object[] values = new Object[ctx.values.length];

    for (int i = 0; i < values.length; i++) {
      switch (ctx.values[i].type) {
        case I32:
        values[i] = ctx.values[i].i32();
        break;

        case I64:
        values[i] = ctx.values[i].i64();
        break;

        case F32:
        values[i] = ctx.values[i].f32();
        break;

        case F64:
        values[i] = ctx.values[i].f64();
        break;

        default:
        throw new RuntimeException("Unreachable");
      }
    }

    Message.PrettyBroadcast(values);

    return new Value<?>[0];
  }

  public static Value<?>[] logString(ImportCallCtx ctx) {
    List<Long> ptr = new ArrayList<>(ctx.values.length);

    for (int i = 0; i < ctx.values.length; i++) {
      Value<?> v = ctx.values[i];

      if (v.type == WasmType.I32) {
        ptr.add((long)v.i32());
      } else {
        ptr.add(v.i64());
      }
    }

    // try {
    //   String str = ctx.Module.ReadString(ptr);
    //   System.out.println(str);
    //   Message.PrettyBroadcast(new Object[] {str});
    // } catch (WasmtimeException e) {
    //   throw new RuntimeException("Failed to read a string while trying to log something: " + e.getMessage());
    // }

    return new Value<?>[0];
  }
}
