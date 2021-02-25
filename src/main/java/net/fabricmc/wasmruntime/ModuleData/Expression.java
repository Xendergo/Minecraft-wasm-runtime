package net.fabricmc.wasmruntime.ModuleData;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.wasmruntime.ModuleExecutor.Instruction;

public class Expression {
  public Instruction[] bytecode;
  public int stackSize = -1;
  public FunctionType type;

  public List<Expression> Blocks = new ArrayList<Expression>();

  public Expression(Instruction[] bytecodeOof, List<Expression> BlocksOof) {
    bytecode = bytecodeOof;
    Blocks = BlocksOof;
  }

  // Will do this later
  public boolean IsValid(boolean isConstant) {
    stackSize = 1;
    return true;
  }

  public String toString() {
    return "Expression {type: " + type + ", stackSize: " + stackSize + ", bytecode: " + bytecode + "}";
  }
}
