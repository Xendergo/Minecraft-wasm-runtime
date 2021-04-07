package wasmruntime.CarpetStuff;

import carpet.script.value.Value;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
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
  public Tag toTag(boolean force) {
    return StringTag.of(getString());
  }

  @Override
  public String getTypeString() {
    return "WasmModule";
  }
}
