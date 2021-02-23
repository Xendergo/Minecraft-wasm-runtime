package net.fabricmc.wasmruntime.ModuleData;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.wasmruntime.ModuleData.HelpfulEnums.WasmType;

public class Code {
  List<WasmType> locals = new ArrayList<WasmType>();
  Expression expr;

  public Code(List<WasmType> localsOof, Expression exprOof) {
    locals = localsOof;
    expr = exprOof;
  }

  public String toString() {
    return "Code {locals: " + locals + ", expr: " + expr + "}";
  }
}
