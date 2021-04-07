package wasmruntime.CarpetStuff;

import carpet.CarpetExtension;
import carpet.script.CarpetExpression;
import carpet.script.Expression;
import carpet.script.exception.InternalExpressionException;
import carpet.script.value.StringValue;
import wasmruntime.Modules;

public class Extension implements CarpetExtension {
  @Override
  public void scarpetApi(CarpetExpression CarpetExpr) {
    Expression expr = CarpetExpr.getExpr();

    expr.addFunction("get_module", (params) -> {
      if (params.size() == 0 || !(params.get(0) instanceof StringValue)) throw new InternalExpressionException("Must provide a string argument");
      
      String name = ((StringValue)params.get(0)).getString();

      if (!Modules.modules.containsKey(name)) throw new InternalExpressionException("There's no module with that name loaded");

      return new ModuleValue(Modules.modules.get(name));
    });
  }
}
