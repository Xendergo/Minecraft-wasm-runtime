package wasmruntime.CarpetStuff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.script.CarpetContext;
import carpet.script.CarpetExpression;
import carpet.script.Context;
import carpet.script.Expression;
import carpet.script.argument.FunctionArgument;
import carpet.script.exception.InternalExpressionException;
import carpet.script.value.FunctionValue;
import carpet.script.value.ListValue;
import carpet.script.value.NumericValue;
import carpet.script.value.StringValue;
import wasmruntime.ModuleWrapper;
import wasmruntime.Modules;
import wasmruntime.Exceptions.WasmtimeEmbeddingException;
import wasmruntime.Types.FuncSignature;
import wasmruntime.Types.Value;

public class Extension implements CarpetExtension {
    public static void LoadExtension() {
        CarpetServer.manageExtension(new Extension());
    }

    @Override
    public void scarpetApi(CarpetExpression carpetExpr) {
        Expression expr = carpetExpr.getExpr();

        expr.addFunction("get_module", (params) -> {
            if (params.isEmpty() || !(params.get(0) instanceof StringValue))
                throw new InternalExpressionException("Must provide a module name");

            String name = ((StringValue) params.get(0)).getString();

            if (!Modules.moduleExists(name))
                throw new InternalExpressionException("There's no module with that name loaded");

            return new ModuleValue(Modules.getModule(name));
        });

        expr.addContextFunction("load_module", -1, (ctx, i, params) -> {
            if (params.isEmpty() || !(params.get(0) instanceof StringValue))
                throw new InternalExpressionException("Must provide a module name");

            String name = ((StringValue) params.get(0)).getString();

            if (!Modules.moduleExists(name)) {
                try {
                    Modules.LoadModule(((CarpetContext) ctx).s.getServer(), name);
                } catch (WasmtimeEmbeddingException e) {
                    throw new InternalExpressionException(e.getMessage());
                }
            }

            System.out.println(Modules.allModules());

            return new ModuleValue(Modules.getModule(name));
        });

        expr.addContextFunction("reload_module", -1, (ctx, i, params) -> {
            if (params.isEmpty() || !(params.get(0) instanceof StringValue))
                throw new InternalExpressionException("Must provide a module name");

            String name = ((StringValue) params.get(0)).getString();

            try {
                Modules.LoadModule(((CarpetContext) ctx).s.getServer(), name);
            } catch (WasmtimeEmbeddingException e) {
                throw new InternalExpressionException(e.getMessage());
            }

            return new ModuleValue(Modules.getModule(name));
        });

        expr.addFunction("call_wasm_function", (params) -> {
            AssertModuleFunctionParams(params);

            ModuleWrapper module = ((ModuleValue) params.get(0)).module;
            String name = ((StringValue) params.get(1)).getString();

            if (!module.exportedFunctions.containsKey(name))
                throw new InternalExpressionException("The module doesn't export a function named " + name);

            FuncSignature type = module.exportedFunctions.get(name);
            int argAmt = type.inputs.length;

            if (argAmt > params.size() - 2)
                throw new InternalExpressionException("The function " + name + " requires " + argAmt + " arguments");

            List<Value<?>> inputs = new ArrayList<>();

            for (int i = 0; i < argAmt; i++) {
                inputs.add(Value.parseScarpet(params.get(i + 2), type.inputs[i]));
            }

            List<Value<?>> ret;
            try {
                ret = module.CallExport(name, inputs);
            } catch (Exception e) {
                throw new InternalExpressionException("Wasm trapped: " + e.getMessage());
            }

            return ListValue.wrap(ret.stream().map(Value::intoScarpet).toList());
        });

        expr.addContextFunction("provide_import", -1, (c, i, params) -> {
            int paramAmt = params.size();
            if (paramAmt < 3)
                throw new InternalExpressionException(
                        "Must provide a wasm module, a function name, & a scarpet function to call back");

            AssertModuleFunctionParams(params);

            if (!(params.get(2) instanceof StringValue || params.get(2) instanceof FunctionValue))
                throw new InternalExpressionException("Must provide a scarpet function or function name");

            FunctionArgument provided = FunctionArgument.findIn(c, expr.module, params, 2, false, false);

            ModuleWrapper module = ((ModuleValue) params.get(0)).module;

            module.importedFunctions.put(((StringValue) params.get(1)).getString(), (ctx) -> {
                var scarpetRet = provided.function.callInContext(c, Context.Type.NONE,
                        Arrays.stream(ctx.values).map(Value::intoScarpet).toList()).evalValue(c);

                if (ctx.expectedType.outputs.length > 0) {
                    return new Value<?>[] { Value.parseScarpet(
                            scarpetRet,
                            ctx.expectedType.outputs[0]) };
                } else {
                    return new Value[0];
                }
            });

            return new NumericValue(0);
        });
    }

    private static void AssertModuleFunctionParams(List<carpet.script.value.Value> params) {
        if (params.size() < 2 || !(params.get(0) instanceof ModuleValue) || !(params.get(1) instanceof StringValue))
            throw new InternalExpressionException("Must provide a module and a function name");
    }
}
