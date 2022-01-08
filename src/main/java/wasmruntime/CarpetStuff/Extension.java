package wasmruntime.CarpetStuff;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.script.CarpetExpression;
import carpet.script.Context;
import carpet.script.Expression;
import carpet.script.LazyValue;
import carpet.script.Context;
import carpet.script.argument.FunctionArgument;
import carpet.script.exception.InternalExpressionException;
import carpet.script.value.FunctionValue;
import carpet.script.value.ListValue;
import carpet.script.value.NumericValue;
import carpet.script.value.StringValue;
import wasmruntime.ModuleWrapper;
import wasmruntime.Modules;
import wasmruntime.ModuleImports;
import wasmruntime.Enums.WasmType;
import wasmruntime.Exceptions.WasmtimeException;
import wasmruntime.Types.FuncType;
import wasmruntime.Types.Value;
import wasmruntime.Utils.ImportCallCtx;

public class Extension implements CarpetExtension {
  public static void LoadExtension() {
    CarpetServer.manageExtension(new Extension());
  }

  @Override
  public void scarpetApi(CarpetExpression CarpetExpr) {
    Expression expr = CarpetExpr.getExpr();

    expr.addFunction("get_module", (params) -> {
      if (params.size() == 0 || !(params.get(0) instanceof StringValue)) throw new InternalExpressionException("Must provide a module name");
      
      String name = ((StringValue)params.get(0)).getString();

      if (!Modules.modules.containsKey(name)) throw new InternalExpressionException("There's no module with that name loaded");

      return new ModuleValue(Modules.modules.get(name));
    });

    expr.addFunction("load_module", (params) -> {
      if (params.size() == 0 || !(params.get(0) instanceof StringValue)) throw new InternalExpressionException("Must provide a module name");

      String name = ((StringValue)params.get(0)).getString();

      if (!Modules.modules.containsKey(name)) {
        try {
          Modules.LoadModule(name);
        } catch (IOException | WasmtimeException e) {
          throw new InternalExpressionException(e.getMessage());
        }
      }

      return new ModuleValue(Modules.modules.get(name));
    });

    expr.addFunction("reload_module", (params) -> {
      if (params.size() == 0 || !(params.get(0) instanceof StringValue)) throw new InternalExpressionException("Must provide a module name");

      String name = ((StringValue)params.get(0)).getString();

      try {
        Modules.LoadModule(name);
      } catch (IOException | WasmtimeException e) {
        throw new InternalExpressionException(e.getMessage());
      }

      return new ModuleValue(Modules.modules.get(name));
    });

    expr.addFunction("call_wasm_function", (params) -> {
      AssertModuleFunctionParams(params);

      ModuleWrapper module = ((ModuleValue)params.get(0)).module;
      String name = ((StringValue)params.get(1)).getString();

      if (!module.exportedFunctions.containsKey(name)) throw new InternalExpressionException("The module doesn't export a function named " + name);

      FuncType type = module.exportedFunctions.get(name);
      int argAmt = type.inputs.length;

      List<Value<?>> inputs = ScarpetToWasm(params.stream().skip(2).collect(Collectors.toList()), type.inputs);

      if (argAmt != inputs.size()) throw new InternalExpressionException("The function " + name + " requires " + argAmt + " arguments");

      List<Value<?>> ret;
      try {
        ret = module.CallFunction(name, inputs);
      } catch (Exception e) {
        throw new InternalExpressionException("Wasm trapped: " + e.getMessage());
      }

      return ListValue.wrap(WasmToScarpet(ret));
    });

    expr.addContextFunction("provide_import", -1, (c, i, params) -> {
      int paramAmt = params.size();
      if (paramAmt < 3) throw new InternalExpressionException("Must provide a wasm module, a function name, & a scarpet function to call back");

      AssertModuleFunctionParams(params);

      if (!(params.get(2) instanceof StringValue || params.get(2) instanceof FunctionValue)) throw new InternalExpressionException("Must provide a scarpet function or function name");

      FunctionArgument provided = FunctionArgument.findIn(c, expr.module, params, 2, false, false);

      ModuleWrapper module = ((ModuleValue)params.get(0)).module;

      if (!ModuleImports.perModuleImports.containsKey(module.moduleName)) ModuleImports.perModuleImports.put(module.moduleName, new HashMap<String, Function<ImportCallCtx, Value<?>[]>>());

      ModuleImports.perModuleImports.get(module.moduleName).put(((StringValue)params.get(1)).getString(), (ctx) -> {
        return ScarpetToWasm(provided.function.callInContext(c, Context.Type.NONE, WasmToScarpet(ctx.values)).evalValue(c), ctx.expectedType.outputs).toArray(new Value<?>[0]);
      });

      return new NumericValue(0);
    });

    expr.addFunction("read_string", (params) -> {
      if (params.size() < 2 || !(params.get(0) instanceof ModuleValue) || !(params.get(1) instanceof ListValue))
        throw new InternalExpressionException("Must provide a module and string pointer");
      
      ModuleWrapper module = ((ModuleValue) params.get(0)).module;

      List<Long> value = ((ListValue) params.get(1)).getItems().stream().map(v -> ((NumericValue)v).getLong()).collect(Collectors.toList());

      try {
        return new StringValue(module.ReadString(value));
      } catch (WasmtimeException e) {
        throw new InternalExpressionException("Wasm trapped: " + e.getMessage());
      }
    });

    expr.addFunction("new_string", (params) -> {
      if (params.size() < 2 || !(params.get(0) instanceof ModuleValue) || !(params.get(1) instanceof StringValue))
        throw new InternalExpressionException("Must provide a module and string");
      
      ModuleWrapper module = ((ModuleValue) params.get(0)).module;

      try {
        return new ListValue(module.NewString(((StringValue)params.get(1)).getString()).stream().map((v) -> new NumericValue(v)).collect(Collectors.toList()));
      } catch (WasmtimeException e) {
        throw new InternalExpressionException("Wasm trapped: " + e.getMessage());
      }
    });
  }

  private static void AssertModuleFunctionParams(List<carpet.script.value.Value> params) {
    if (params.size() < 2 || !(params.get(0) instanceof ModuleValue) || !(params.get(1) instanceof StringValue)) throw new InternalExpressionException("Must provide a module and a function name");
  }

  public List<Value<?>> ScarpetToWasm(carpet.script.value.Value scarpetArg, WasmType[] inputTypes) {
    return ScarpetToWasm(Arrays.asList(scarpetArg), inputTypes);
  }

  public List<Value<?>> ScarpetToWasm(List<carpet.script.value.Value> scarpetArgs, WasmType[] inputTypes) {
    List<Value<?>> ret = new ArrayList<Value<?>>();

    int i = 0;
    for (carpet.script.value.Value value : scarpetArgs) {
      if (value instanceof NumericValue) {
        NumericValue v2 = (NumericValue) value;
        switch (inputTypes[i]) {
          case I32:
          ret.add(Value.fromI32(v2.getInt()));
          break;

          case I64:
          ret.add(Value.fromI64(v2.getLong()));
          break;

          case F32:
          ret.add(Value.fromF32(v2.getFloat()));
          break;

          case F64:
          ret.add(Value.fromF64(v2.getDouble()));
          break;

          default:
          throw new InternalExpressionException("Using a non-numeric wasm type in scarpet is unsupported");
        }

        i++;
      } else if (value instanceof ListValue) {
        List<carpet.script.value.Value> values = ((ListValue) value).getItems();

        ret.addAll(ScarpetToWasm(values, ArrayUtils.subarray(inputTypes, i, i + values.size())));
        i += values.size();
      } else {
        throw new InternalExpressionException("Scarpet value must have a numeric type");
      }
    }

    return ret;
  }

  public List<carpet.script.value.Value> WasmToScarpet(Value<?>[] values) {
    return WasmToScarpet(Arrays.asList(values));
  }

  public List<carpet.script.value.Value> WasmToScarpet(List<Value<?>> values) {
    List<carpet.script.value.Value> ret = new ArrayList<>();

    for (Value<?> v : values) {
      switch (v.type) {
        case I32:
        ret.add(NumericValue.of(v.i32()));
        break;

        case I64:
        ret.add(NumericValue.of(v.i64()));
        break;

        case F32:
        ret.add(NumericValue.of(v.f32()));
        break;

        case F64:
        ret.add(NumericValue.of(v.f64()));
        break;

        default:
        throw new InternalExpressionException("Using a non-numeric wasm type in scarpet is unsupported");
      }
    }

    return ret;
  }

  // private static List<LazyValue> ToLazy(List<carpet.script.value.Value> values) {
  //   List<LazyValue> ret = new ArrayList<>(values.size());

  //   for (carpet.script.value.Value value : values) {
  //     ret.add((c, t) -> value);
  //   }

  //   return ret;
  // }

  // private static List<carpet.script.value.Value> ToValues(List<LazyValue> values, Context c) {
  //   List<carpet.script.value.Value> ret = new ArrayList<>(values.size());

  //   for (LazyValue value : values) {
  //     ret.add(value.evalValue(c));
  //   }

  //   return ret;
  // }
}
