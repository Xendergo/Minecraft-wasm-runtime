package wasmruntime.ModuleExecutor;

import wasmruntime.ModuleData.HelpfulEnums.WasmType;

public class Instruction {
  public InstructionType operation;
  public Value[] immediates;
  public WasmType typeAnnotation;

  public Instruction(InstructionType operationOof, Value[] immediatesOof) {
    operation = operationOof;
    immediates = immediatesOof;
  }

  public Instruction(InstructionType operationOof, Value[] immediatesOof, WasmType typeAnnotationOof) {
    operation = operationOof;
    immediates = immediatesOof;
    typeAnnotation = typeAnnotationOof;
  }

  public String toString() {
    return operation + " " + immediates;
  }
}
