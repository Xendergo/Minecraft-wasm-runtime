package net.fabricmc.wasmruntime.ModuleData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.fabricmc.wasmruntime.ModuleData.HelpfulEnums.GenericTypeRequirers;
import net.fabricmc.wasmruntime.ModuleData.HelpfulEnums.WasmType;
import net.fabricmc.wasmruntime.ModuleExecutor.Instruction;
import net.fabricmc.wasmruntime.ModuleExecutor.InstructionType;
import net.fabricmc.wasmruntime.ModuleExecutor.ValueI32;
import net.fabricmc.wasmruntime.ModuleExecutor.ValueStack;

public class Expression {
  public Instruction[] bytecode;
  public int stackSize = -1;
  public FunctionType type;
  public boolean isBlock = false;
  public boolean isLoop = false;

  public WasmType[] locals;

  public List<Expression> Blocks = new ArrayList<Expression>();

  public Expression(Instruction[] bytecodeOof, List<Expression> BlocksOof) {
    bytecode = bytecodeOof;
    Blocks = BlocksOof;
    isBlock = false;
  }

  public Expression(Instruction[] bytecodeOof, List<Expression> BlocksOof, WasmType[] localsOof) {
    bytecode = bytecodeOof;
    Blocks = BlocksOof;
    isBlock = true;
    locals = localsOof;
  }

  // Will do this later
  public boolean IsValid(boolean isConstant, Global<?>[] globals) {
    try {
      System.out.println("Started validating");
      LinkedList<WasmType> typeStack = new LinkedList<WasmType>();

      if (!isBlock) {
        locals = type.inputs;
      } else {
        for (WasmType input : type.inputs) {
          typeStack.add(input);
        }
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
            genericType = locals[((ValueI32)instr.immediates.get(0)).value];
            break;

            case global:
            genericType = globals[((ValueI32)instr.immediates.get(0)).value].type;
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
          Expression block = Blocks.get(((ValueI32)instr.immediates.get(0)).value);
          block.isBlock = true;
          block.locals = locals;
          if (!block.IsValid(isConstant, globals)) return false;
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

        System.out.println(typeStack);
      }

      System.out.println("done validating");

      stackSize += 3; // Solves an edge case where the stack size is largest before the first operation is executed
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }

    return true;
  }

  /*
  TODO: this
  */
  public void enterBlock(ValueStack stack, int block) {
    
  }

  public void enterBlock(ValueStack stack) {
    enterBlock(stack, ((ValueI32)Opcodes.immediates[0]).value);
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
