package net.fabricmc.wasmruntime.ModuleExecutor;

import java.util.List;

public class Instruction {
  public InstructionType operation;
  public List<Value> immediates;

  public Instruction(InstructionType operationOof, List<Value> immediatesOof) {
    operation = operationOof;
    immediates = immediatesOof;
  }

  public String toString() {
    return operation + " " + immediates;
  }
}
