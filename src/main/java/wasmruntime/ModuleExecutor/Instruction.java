package wasmruntime.ModuleExecutor;

public class Instruction {
  public InstructionType operation;
  public Value[] immediates;

  public Instruction(InstructionType operationOof, Value[] immediatesOof) {
    operation = operationOof;
    immediates = immediatesOof;
  }

  public String toString() {
    return operation + " " + immediates;
  }
}
