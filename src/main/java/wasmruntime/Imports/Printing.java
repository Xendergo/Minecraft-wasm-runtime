package wasmruntime.Imports;

import java.util.Arrays;

import wasmruntime.Types.Value;
import wasmruntime.Utils.ImportCallCtx;
import wasmruntime.Utils.Message;

public class Printing {
    public static Value<?>[] log(ImportCallCtx ctx) {
        Object[] values = new Object[ctx.values.length];

        for (int i = 0; i < values.length; i++) {
            values[i] = ctx.values[i].value;
        }

        Message.PrettyBroadcast(ctx.Module.server, values);

        return new Value<?>[0];
    }

    public static Value<?>[] logString(ImportCallCtx ctx) {
        String str = ctx.values[0].string();
        Message.PrettyBroadcast(ctx.Module.server, new Object[] { str });

        return new Value<?>[0];
    }
}
