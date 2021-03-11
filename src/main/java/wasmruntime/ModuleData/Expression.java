package wasmruntime.ModuleData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import wasmruntime.Errors.Trap;
import wasmruntime.Errors.TrapRuntime;
import wasmruntime.ModuleData.HelpfulEnums.GenericTypeRequirers;
import wasmruntime.ModuleData.HelpfulEnums.WasmType;
import wasmruntime.ModuleExecutor.ExecExpression;
import wasmruntime.ModuleExecutor.Instruction;
import wasmruntime.ModuleExecutor.InstructionType;
import wasmruntime.ModuleExecutor.Value;
import wasmruntime.ModuleExecutor.ValueI32;
import wasmruntime.ModuleExecutor.ValueStack;

public class Expression {
  public Instruction[] bytecode;
  public int stackSize = -1;
  public FunctionType type;
  public boolean isBlock = false;
  public boolean isLoop = false;

  public WasmType[] localTypes;
  public Value[] locals;

  public List<Expression> Blocks = new ArrayList<Expression>();

  public Expression(Instruction[] bytecodeOof, List<Expression> BlocksOof) {
    bytecode = bytecodeOof;
    Blocks = BlocksOof;
    isBlock = false;
  }

  public Expression(Instruction[] bytecodeOof, List<Expression> BlocksOof, WasmType[] locals) {
    bytecode = bytecodeOof;
    Blocks = BlocksOof;
    isBlock = true;
    localTypes = locals;
  }

  public boolean IsValid(boolean isConstant, Global<?>[] globals) {
    try {
      LinkedList<WasmType> typeStack = new LinkedList<WasmType>();

      WasmType[] thisLocals;

      if (isBlock) {
        for (WasmType input : type.inputs) {
          typeStack.add(input);
        }

        thisLocals = localTypes;
      } else {
        thisLocals = ArrayUtils.addAll(type.inputs, localTypes);
      }

      for (int i = 0; i < bytecode.length; i++) {
        InstructionType op = bytecode[i].operation;
        Instruction instr = bytecode[i];

        FunctionType instrType = bytecode[i].operation.type;

        if (op.genericTypeUse != GenericTypeRequirers.none) {
          instrType = new FunctionType(instrType);

          WasmType genericType;

          switch (op.genericTypeUse) {
            case local:
            genericType = thisLocals[((ValueI32)instr.immediates[0]).value];
            break;

            case global:
            genericType = globals[((ValueI32)instr.immediates[0]).value].type;
            break;

            case select:
            genericType = typeStack.get(typeStack.size() - 1);
            break;

            default:
            // This should be unreachable
            System.out.println("Something's terribly broken when finding generic type");
            return false;
          }

          
          for (int j = 0; j < instrType.inputs.length; j++) {
            if (instrType.inputs[j] == WasmType.T) {
              instrType.inputs[j] = genericType;
            }
          }
          
          for (int j = 0; j < instrType.outputs.length; j++) {
            if (instrType.outputs[j] == WasmType.T) {
              instrType.outputs[j] = genericType;
            }
          }
        }
        
        if (op.invokesBlock) {
          for (Value immediate : instr.immediates) {
            Expression block = Blocks.get(Opcodes.i32(immediate));
            block.isBlock = true;
            block.localTypes = thisLocals;
            if (!block.IsValid(isConstant, globals)) return false;
          }
        }

        for (int j = instrType.inputs.length - 1; j >= 0; j--) {
          if (typeStack.pollFirst() != instrType.inputs[j]) {
            return false; // type is wrong
          }
        }

        for (int j = instrType.outputs.length - 1; j >= 0; j--) {
          typeStack.addFirst(instrType.outputs[j]);
        }

        if (stackSize < typeStack.size()) {
          stackSize = typeStack.size();
        }

        if (op.stopsExecution) {
          break;
        }
      }

      if (type.outputs.length != typeStack.size() && !isBlock) return false; // Output is wrong

      for (int i = 0; i < type.outputs.length; i++) {
        if (typeStack.pollFirst() != type.outputs[i]) return false; // Output is wrong
      }

      stackSize = Math.max(stackSize, type.inputs.length);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }

    return true;
  }

  public void localGet(ValueStack stack) {
    stack.push(locals[Opcodes.i32(Opcodes.immediates[0])]);
  }

  public void localSet(ValueStack stack) {
    locals[Opcodes.i32(Opcodes.immediates[0])] = stack.pop();
  }

  public void localTee(ValueStack stack) {
    locals[Opcodes.i32(Opcodes.immediates[0])] = stack.peek();
  }

  public void enterBlock(ValueStack stack, int blockIndex) {
    Expression block = Blocks.get(blockIndex);
    try {
      ValueStack ret = ExecExpression.Exec(block, stack.module, locals, new ValueStack(block.stackSize, stack.module, block.type.popArgs(stack)));
      
      if (ExecExpression.branchDepth == -1) {
        stack.pushStack(block.type.popOutput(ret));
      }
    } catch (Trap trap) {
      throw new TrapRuntime(trap.getMessage());
    }
  }

  public void enterBlock(ValueStack stack) {
    enterBlock(stack, Opcodes.i32(Opcodes.immediates[0]));
  }

  public void enterBlockIf(ValueStack stack) {
    if (((ValueI32)stack.pop()).value != 0) {
      enterBlock(stack);
    }
  }

  public void enterBlockIfElse(ValueStack stack) {
    if (((ValueI32)stack.pop()).value == 0) {
      enterBlock(stack, ((ValueI32)Opcodes.immediates[1]).value);
    } else {
      enterBlock(stack);
    }
  }

  public String toString() {
    return "Expression {type: " + type + ", stackSize: " + stackSize + ", bytecode: " + bytecode + "}";
  }
}
