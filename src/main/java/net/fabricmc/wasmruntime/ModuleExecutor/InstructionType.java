package net.fabricmc.wasmruntime.ModuleExecutor;

import java.util.function.Consumer;

import net.fabricmc.wasmruntime.ModuleData.FunctionType;
import net.fabricmc.wasmruntime.ModuleData.HelpfulEnums.WasmType;

public class InstructionType {
  Consumer<ValueStack> operation;
  FunctionType type;
  WasmType[] immediates;

  public InstructionType(Consumer<ValueStack> operationOof, FunctionType typeOof, WasmType[] immediatesOof) {
    operation = operationOof;
    type = typeOof;
    immediates = immediatesOof;
  }
}
