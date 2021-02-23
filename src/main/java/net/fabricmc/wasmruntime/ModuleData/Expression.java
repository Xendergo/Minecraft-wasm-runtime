package net.fabricmc.wasmruntime.ModuleData;

import java.util.ArrayList;
import java.util.List;

public class Expression {
  public byte[] bytecode;
  public int stackSize = -1;
  public FunctionType type;

  public List<Expression> Blocks = new ArrayList<Expression>();

  public Expression(byte[] bytecodeOof, FunctionType typeOof) {
    bytecode = bytecodeOof;
    type = typeOof;
  }

  // Will do this later
  public boolean IsValid() {
    stackSize = 1;
    return true;
  }
}
