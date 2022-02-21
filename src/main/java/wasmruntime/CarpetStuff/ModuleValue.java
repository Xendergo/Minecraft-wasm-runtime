package wasmruntime.CarpetStuff;

import carpet.script.value.Value;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import wasmruntime.ModuleWrapper;

public class ModuleValue extends Value {
    public ModuleWrapper module;

    public ModuleValue(ModuleWrapper module) {
        this.module = module;
    }

    @Override
    public String getString() {
        return module.moduleName;
    }

    @Override
    public boolean getBoolean() {
        return module != null;
    }

    @Override
    public NbtElement toTag(boolean force) {
        return NbtString.of(getString());
    }

    @Override
    public String getTypeString() {
        return "WasmModule";
    }
}
