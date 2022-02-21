package wasmruntime.Types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// type signature of a wasm function -matgenius04
public class FuncSignature {
    public InteroperableType[] inputs;
    public InteroperableType[] outputs;

    public FuncSignature(InteroperableType[] inputs, InteroperableType[] outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public FuncSignature(List<Byte> bytes) {
        List<InteroperableType> inputs = new ArrayList<>();
        List<InteroperableType> outputs = new ArrayList<>();

        boolean using_inputs = true;

        for (Byte id : bytes) {
            if (id == 0) {
                using_inputs = false;
                continue;
            }

            if (using_inputs) {
                inputs.add(InteroperableType.idMap.get(id));
            } else {
                outputs.add(InteroperableType.idMap.get(id));
            }
        }

        this.inputs = inputs.toArray(new InteroperableType[0]);
        this.outputs = outputs.toArray(new InteroperableType[0]);
    }

    public static FuncSignature FromList(List<Byte> bytes) {
        return new FuncSignature(bytes);
    }

    @Override
    public String toString() {
        return Arrays.toString(inputs) + " => " + Arrays.toString(outputs);
    }
}
