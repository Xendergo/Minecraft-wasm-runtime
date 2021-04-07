package wasmruntime.CarpetStuff;

import java.util.ArrayList;
import java.util.List;

import carpet.CarpetExtension;
import carpet.script.CarpetExpression;
import carpet.script.Expression;
import carpet.script.exception.InternalExpressionException;
import carpet.script.value.ListValue;
import carpet.script.value.NumericValue;
import carpet.script.value.StringValue;
import wasmruntime.ModuleWrapper;
import wasmruntime.Modules;
import wasmruntime.Types.FuncType;
import wasmruntime.Types.Value;

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

    expr.addFunction("call_wasm_function", (params) -> {
      if (params.size() < 2 || !(params.get(0) instanceof ModuleValue) || !(params.get(1) instanceof StringValue)) throw new InternalExpressionException("Must provide a module and a function name");

      ModuleWrapper module = ((ModuleValue)params.get(0)).module;
      String name = ((StringValue)params.get(1)).getString();

      if (!module.exportedFunctions.containsKey(name)) throw new InternalExpressionException("The module doesn't export a function named " + name);

      FuncType type = module.exportedFunctions.get(name);
      int argAmt = type.inputs.length;
      if (argAmt != params.size() - 2) throw new InternalExpressionException("The function " + name + " requires " + argAmt + " arguments");

      List<Value<?>> inputs = new ArrayList<Value<?>>(argAmt);

      for (int i = 0; i < argAmt; i++) {
        carpet.script.value.Value v = params.get(i + 2);

        if (v instanceof NumericValue) {
          NumericValue v2 = (NumericValue) v;
          switch (type.inputs[i]) {
            case I32:
            inputs.add(Value.fromI32(v2.getInt()));
            break;

            case I64:
            inputs.add(Value.fromI64(v2.getLong()));
            break;

            case F32:
            inputs.add(Value.fromF32(v2.getFloat()));
            break;

            case F64:
            inputs.add(Value.fromF64(v2.getDouble()));
            break;

            default:
            throw new InternalExpressionException("Calling a function with a non-numeric argument is unsupported");
          }
        } else {
          throw new InternalExpressionException("Argument must have a numeric type");
        }
      }

      List<Value<?>> ret;
      try {
        ret = module.CallFunction(name, inputs);
      } catch (Exception e) {
        throw new InternalExpressionException("Wasm trapped: " + e.getMessage());
      }

      List<carpet.script.value.Value> scarpyRet = new ArrayList<carpet.script.value.Value>(ret.size());

      for (Value<?> v : ret) {
        switch (v.type) {
          case I32:
          scarpyRet.add(NumericValue.of(v.i32()));
          break;

          case I64:
          scarpyRet.add(NumericValue.of(v.i64()));
          break;

          case F32:
          scarpyRet.add(NumericValue.of(v.f32()));
          break;

          case F64:
          scarpyRet.add(NumericValue.of(v.f64()));
          break;

          default:
          throw new InternalExpressionException("Couldn't convert non-number return type to a number");
        }
      }

      return ListValue.wrap(scarpyRet);
    });
  }
}
