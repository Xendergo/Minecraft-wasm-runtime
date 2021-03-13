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

  public boolean IsValid(Module module) {
    return expr.IsValid(false, module);
  }

  public String toString() {
    return "Code {locals: " + locals + ", expr: " + expr + "}";
  }
}
