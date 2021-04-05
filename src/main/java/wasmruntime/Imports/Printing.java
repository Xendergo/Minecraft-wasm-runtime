package wasmruntime.Imports;

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
}
