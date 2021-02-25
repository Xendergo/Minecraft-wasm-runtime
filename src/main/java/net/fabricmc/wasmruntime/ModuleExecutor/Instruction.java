package net.fabricmc.wasmruntime.ModuleExecutor;

import java.util.List;

public class Instruction {
  InstructionType operation;
  List<Value> immediates;

  public Instruction(InstructionType operationOof, List<Value> immediatesOof) {
    operation = operationOof;
    immediates = immediatesOof;
  }

  public String toString() {
    return operation + " " + immediates;
  }
}
