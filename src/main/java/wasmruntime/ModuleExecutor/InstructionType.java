package wasmruntime.ModuleExecutor;

import java.util.function.Consumer;

import wasmruntime.ModuleData.FunctionType;
import wasmruntime.ModuleData.HelpfulEnums.GenericTypeRequirers;
import wasmruntime.ModuleData.HelpfulEnums.WasmType;

public class InstructionType {
  Consumer<ValueStack> operation;
  public FunctionType type;
  public WasmType[] immediates;
  public GenericTypeRequirers genericTypeUse = GenericTypeRequirers.none;
  public boolean invokesBlock = false;

  public InstructionType(Consumer<ValueStack> operationOof, FunctionType typeOof, WasmType[] immediatesOof) {
    operation = operationOof;
    type = typeOof;
    immediates = immediatesOof;
  }

  public InstructionType(Consumer<ValueStack> operationOof, FunctionType typeOof, WasmType[] immediatesOof, GenericTypeRequirers genericTypeUseOof) {
    operation = operationOof;
    type = typeOof;
    immediates = immediatesOof;
    genericTypeUse = genericTypeUseOof;
  }

  public InstructionType(Consumer<ValueStack> operationOof, FunctionType typeOof, WasmType[] immediatesOof, boolean invokesBlockOof) {
    operation = operationOof;
    type = typeOof;
    immediates = immediatesOof;
    invokesBlock = invokesBlockOof;
  }

  @Override
  public String toString() {
    return "type: " + type;
  }
}