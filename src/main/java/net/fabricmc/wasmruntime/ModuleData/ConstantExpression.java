package net.fabricmc.wasmruntime.ModuleData;

import net.fabricmc.wasmruntime.ModuleData.HelpfulEnums.WasmType;

public class ConstantExpression extends Expression {
  public ConstantExpression(byte[] bytecodeOof, WasmType type) {
    super(bytecodeOof, new FunctionType(type));
  }

  // Will do later
  @Override
  public boolean IsValid() {
    return super.IsValid();
  }
}
