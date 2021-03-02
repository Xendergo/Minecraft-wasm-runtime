package wasmruntime.ModuleData;

import java.util.ArrayList;
import java.util.List;

import wasmruntime.ModuleData.HelpfulEnums.WasmType;

public class Code {
  List<WasmType> locals = new ArrayList<WasmType>();
  public Expression expr;

  public Code(List<WasmType> localsOof, Expression exprOof) {
    locals = localsOof;
    expr = exprOof;
  }

  public boolean IsValid(Global<?>[] globals) {
    return expr.IsValid(false, globals);
  }

  public String toString() {
    return "Code {locals: " + locals + ", expr: " + expr + "}";
  }
}
