package wasmruntime.ModuleParsing;

import wasmruntime.ModuleData.BlockType;
import wasmruntime.ModuleData.Code;
import wasmruntime.ModuleData.Data;
import wasmruntime.ModuleData.Export;
import wasmruntime.ModuleData.Expression;
import wasmruntime.ModuleData.FunctionType;
import wasmruntime.ModuleData.Global;
import wasmruntime.ModuleData.ImportedFunction;
import wasmruntime.ModuleData.ImportedGlobal;
import wasmruntime.ModuleData.Limit;
import wasmruntime.ModuleData.Memory;
import wasmruntime.ModuleData.Module;
import wasmruntime.ModuleData.Opcodes;
import wasmruntime.ModuleData.Table;
import wasmruntime.ModuleData.WasmFunction;
import wasmruntime.ModuleData.HelpfulEnums.ExportTypes;
import wasmruntime.ModuleData.HelpfulEnums.GenericTypeRequirers;
import wasmruntime.ModuleData.HelpfulEnums.WasmType;
import wasmruntime.ModuleExecutor.ExecExpression;
import wasmruntime.ModuleExecutor.Instruction;
import wasmruntime.ModuleExecutor.InstructionType;
import wasmruntime.ModuleExecutor.Value;
import wasmruntime.ModuleExecutor.ValueF32;
import wasmruntime.ModuleExecutor.ValueF64;
import wasmruntime.ModuleExecutor.ValueI32;
import wasmruntime.ModuleExecutor.ValueI64;
import wasmruntime.Operations.ControlFlow;
import wasmruntime.Operations.Functions;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import wasmruntime.Errors.Trap;
import wasmruntime.Errors.WasmParseError;

public class Parser {
  private static int offset = 0; // Tells how many bytes a function consumed

  public static Module parseModule(byte[] bytes) throws WasmParseError {
    Module module = new Module();

    if (Parser.read4ByteInt(bytes, 0) != 1836278016) throw new WasmParseError("This file isn't a wasm file");
    if (Parser.read4ByteInt(bytes, 4) != Module.WasmVersion) throw new WasmParseError("This file uses a different version of WASM");

    int index = 8;
    while (true) {
      int section = bytes[index];
      index++;
      int length = readInt(bytes, index);
      index += offset;
      int end = index + length;
      switch (section) {
        case 0: // Custom section: https://webassembly.github.io/spec/core/binary/modules.html#custom-section
        module.CustomSection.put(readName(bytes, index), Arrays.copyOfRange(bytes, index + offset, end));
        break;
        
        case 1: // Type section: https://webassembly.github.io/spec/core/binary/modules.html#type-section
        int typeAmt = readInt(bytes, index);
        index += offset;
        for (int i = 0; i < typeAmt; i++) {
          if (bytes[index] == 0x60) {
            index++;
            
            int inputAmt = readInt(bytes, index);
            index += offset;
            
            WasmType[] in = new WasmType[inputAmt];

            for (int j = 0; j < inputAmt; j++) {
              in[j] = WasmType.typeMap.get(bytes[index]);
              index++;
            }

            int outputAmt = readInt(bytes, index);
            index += offset;

            WasmType[] out = new WasmType[outputAmt];

            for (int j = 0; j < outputAmt; j++) {
              out[j] = WasmType.typeMap.get(bytes[index]);
              index++;
            }

            module.TypeSection.add(new FunctionType(in, out));
          } else {
            throw new WasmParseError("The type section is messed up");
          }
        }
        break;

        case 2: // Import section: https://webassembly.github.io/spec/core/binary/modules.html#import-section
        int importAmt = readInt(bytes, index);
        index += offset;

        for (int i = 0; i < importAmt; i++) {
          String moduleName = readName(bytes, index);
          index += offset;

          String importName = readName(bytes, index);
          index += offset;

          switch (bytes[index]) {
            case 0x00:
            index++;
            module.Functions.add(new ImportedFunction(moduleName, importName, module.TypeSection.get(readInt(bytes, index))));
            index += offset;
            break;

            case 0x01:
            throw new WasmParseError("Importing tables is unsupported");

            case 0x02:
            throw new WasmParseError("Importing memory is unsupported");

            case 0x03:
            index += 2;
            WasmType type = WasmType.typeMap.get(bytes[index-1]);
            boolean mutable = bytes[index] == 0;
            index++;

            module.Globals.add(new ImportedGlobal(moduleName, importName, mutable, type));
            break;

            default:
            throw new WasmParseError("Unknown import descriptor "+bytes[index]);
          }
        }
        break;

        case 3: // Function section https://webassembly.github.io/spec/core/binary/modules.html#function-section
        int functionAmt = readInt(bytes, index);
        index += offset;

        for (int i = 0; i < functionAmt; i++) {
          module.FunctionTypeIndices.add(readInt(bytes, index));
          index += offset;
        }
        break;

        case 4: // Table section https://webassembly.github.io/spec/core/binary/modules.html#table-section
        int tableAmt = readInt(bytes, index);
        index += offset;

        for (int i = 0; i < tableAmt; i++) {
          WasmType type = WasmType.refMap.get(bytes[index]);
          index++;
          Limit limit = readLimit(bytes, index);
          module.Tables.add(new Table(limit, type));
        }
        break;

        case 5: // Memory section https://webassembly.github.io/spec/core/binary/modules.html#memory-section
        int memoryAmt = readInt(bytes, index);
        index += offset;

        for (int i = 0; i < memoryAmt; i++) {
          module.Memories.add(new Memory(readLimit(bytes, index)));
        }
        break;

        case 6: // Global section https://webassembly.github.io/spec/core/binary/modules.html#global-section
        int globalAmt = readInt(bytes, index);
        index += offset;

        for (int i = 0; i < globalAmt; i++) {
          index += 2;
          WasmType type = WasmType.typeMap.get(bytes[index-2]);
          boolean mutable = bytes[index-1] == 0;
          blockTypeStack.addFirst(new BlockType(new FunctionType(type)));
          Expression expr = readExpr(bytes, index, module, new WasmType[0]);
          expr.type = blockTypeStack.pollFirst().type;
          index += offset;

          if (!expr.IsValid(true, module)) {
            throw new WasmParseError("Invalid global initializer with global index " + i);
          }

          Value ret;
          try {
            ret = ExecExpression.Exec(expr, module, new Value[0]).stack[0];
          } catch (Trap trap) {
            throw new WasmParseError("Initialization of global value trapped: " + trap.getMessage());
          }

          module.Globals.add(new Global(ret, mutable, type));
        }
        break;

        case 7: // Export section https://webassembly.github.io/spec/core/binary/modules.html#export-section
        int exportAmt = readInt(bytes, index);
        index += offset;

        for (int i = 0; i < exportAmt; i++) {
          String name = readName(bytes, index);
          index += offset;

          switch (bytes[index]) {
            case 0x00:
            index++;
            module.exportedFunctions.put(name, new Export(ExportTypes.Func, readInt(bytes, index)));
            index += offset;
            break;

            case 0x01:
            index++;
            printWarning("Exporting tables is unneccesary");
            readInt(bytes, index);
            index += offset;
            break;

            case 0x02:
            if (module.exportedMemory != null) {
              throw new WasmParseError("Can't export more than one memory");
            }
            index++;
            module.exportedMemory = new Export(ExportTypes.Memory, readInt(bytes, index));
            index += offset;
            break;

            case 0x03:
            index++;
            module.exportedGlobals.put(name, new Export(ExportTypes.Global, readInt(bytes, index)));
            index += offset;
            break;

            default:
            throw new WasmParseError("Unknown export descriptor " + bytes[index]);
          }
        }
        break;

        case 8: // Start section https://webassembly.github.io/spec/core/binary/modules.html#start-section
        module.startFunction = readInt(bytes, index);
        index += offset;
        break;

        case 9: // Element section https://webassembly.github.io/spec/core/binary/modules.html#element-section
        int elementAmt = readInt(bytes, index);
        index += offset;

        for (int i = 0; i < elementAmt; i++) {
          int tableIndex = readInt(bytes, index);
          index += offset;

          blockTypeStack.addFirst(new BlockType(new FunctionType(WasmType.i32)));
          Expression expr = readExpr(bytes, index, module, new WasmType[0]);
          expr.type = blockTypeStack.pollFirst().type;
          index += offset;

          if (!expr.IsValid(true, module)) {
            throw new WasmParseError("Constant expression for offset of values in table "+tableIndex+" is invalid");
          }

          int tableOffset;
          try {
            tableOffset = ((ValueI32)ExecExpression.Exec(expr, module, new Value[0]).stack[0]).value;
          } catch (Trap trap) {
            throw new WasmParseError("Initialization of table offset trapped: " + trap.getMessage());
          }

          int vecAmt = readInt(bytes, index);
          index += offset;

          for (int j = 0; j < vecAmt; j++) {
            module.Tables.get(tableIndex).values.put(tableOffset+j, readInt(bytes, index));
            index += offset;
          }
        }
        break;

        case 10: // Code section https://webassembly.github.io/spec/core/binary/modules.html#code-section
        int codeAmt = readInt(bytes, index);
        index += offset;

        for (int i = 0; i < codeAmt; i++) {
          readInt(bytes, index);
          index += offset;

          int localsAmt = readInt(bytes, index);
          index += offset;

          List<WasmType> locals = new ArrayList<WasmType>();

          for (int j = 0; j < localsAmt; j++) {
            int amt = readInt(bytes, index);
            index+=offset;

            WasmType type = WasmType.typeMap.get(bytes[index]);

            index++;

            for (int k = 0; k < amt; k++) {
              locals.add(type);
            }
          }

          blockTypeStack.addFirst(new BlockType(module.TypeSection.get(module.FunctionTypeIndices.get(module.Codes.size()))));
          Expression expr = readExpr(bytes, index, module, locals.toArray(new WasmType[0]));
          index += offset;
          expr.type = blockTypeStack.pollFirst().type;

          Code code = new Code(locals, expr);

          module.Functions.add(new WasmFunction(code));

          module.Codes.add(code);
        }
        break;

        case 11: // Data section https://webassembly.github.io/spec/core/binary/modules.html#data-section
        int dataAmt = readInt(bytes, index);
        index += offset;

        for (int i = 0; i < dataAmt; i++) {
          int dataType = bytes[index];
          index++;

          if (dataType == 1) {
            int byteAmt = readInt(bytes, index);
            index += offset;

            module.Datas[i] = new Data(Arrays.copyOfRange(bytes, index, index + byteAmt));

            index += byteAmt;
          } else {
            int memoryIndex;

            if (dataType == 0) {
              memoryIndex = 0;
            } else {
              memoryIndex = readInt(bytes, index);
              index += offset;
            }

            blockTypeStack.addFirst(new BlockType(new FunctionType(WasmType.i32)));
            Expression expr = readExpr(bytes, index, module, new WasmType[0]);
            expr.type = blockTypeStack.pollFirst().type;
            index += offset;
  
            if (!expr.IsValid(true, module)) {
              throw new WasmParseError("Constant expression for offset of data index " + i + "is invalid");
            }
  
            int memoryOffset;
            try {
              memoryOffset = ((ValueI32)ExecExpression.Exec(expr, module, new Value[0]).stack[0]).value;
            } catch (Trap trap) {
              throw new WasmParseError("Initialization of offset in data section trapped: " + trap.getMessage());
            }
  
            int byteAmt = readInt(bytes, index);
            index += offset;
  
            byte[] memory = module.Memories.get(memoryIndex).data;
            byte[] data = Arrays.copyOfRange(bytes, index, index + byteAmt);
            index += byteAmt;

            if (module.Datas != null) {
              module.Datas[i] = new Data(data);
            }
  
            try {
              for (int j = 0; j < byteAmt; j++) {
                memory[j + memoryOffset] = data[j];
              }
            } catch (ArrayIndexOutOfBoundsException e) {
              throw new WasmParseError("Not enough memory to be able to store the data at index " + memoryOffset);
            }
          }
        }
        break;

        case 12:
        module.Datas = new Data[readInt(bytes, index)];
        index += offset;
        break;

        default:
        throw new WasmParseError("Unknown section id " + section);
      }
      index = end;

      if (index >= bytes.length) break;
    }

    System.out.println(module.Codes);

    return module;
  }

  public static int read4ByteInt(byte[] bytes, int start) {
    int ret = 0;
    for (int i = 0; i < 4; i++) {
      ret += bytes[start+i] << (i << 3);
    }

    return ret;
  }

  public static int readInt(byte[] bytes, int start) {
    int result = 0;
    offset = 0;
    while (true) {
      int reading = bytes[start+offset];
      result |= (reading & 0x7F) << (offset * 7);
      offset++;

      if (reading >> 7 == 0)
        break;
    }

    return result;
  }

  public static long readLong(byte[] bytes, int start) {
    long result = 0;
    offset = 0;
    while (true) {
      long reading = bytes[start+offset];
      result |= (reading & 0x7F) << (offset * 7);
      offset++;

      if (reading >> 7 == 0)
        break;
    }

    return result;
  }

  public static float readFloat(byte[] bytes, int start) {
    offset = 4;
    return Float.intBitsToFloat((bytes[start] & 255) | (bytes[start + 1] & 255) << 8 | (bytes[start + 2] & 255) << 16 | (bytes[start + 3] & 255) << 24);
  }

  public static double readDouble(byte[] bytes, int start) {
    offset = 8;
    return Double.longBitsToDouble(((long) bytes[start] & 255) | ((long) bytes[start + 1] & 255) << 8 | ((long) bytes[start + 2] & 255) << 16 ^ ((long) bytes[start + 3] & 255) << 24 | ((long) bytes[start + 4] & 255) << 32 | ((long) bytes[start + 5] & 255) << 40 | ((long) bytes[start + 6] & 255) << 48 | ((long) bytes[start + 7] & 255) << 56);
  }

  public static String readName(byte[] bytes, int start) {
    int length = readInt(bytes, start);
    start += offset;
    offset += length;
    return new String(Arrays.copyOfRange(bytes, start, start + length), Charset.forName("UTF8"));
  }

  public static Limit readLimit(byte[] bytes, int start) {
    int originalStart = start;

    boolean maxPresent = bytes[start] == 1;
    start++;

    int min = readInt(bytes, start);
    start += offset;
    int max = maxPresent ? readInt(bytes, start) : -1;
    offset = start - originalStart + offset;

    return new Limit(min, max);
  }

  public static FunctionType readBlockType(byte[] bytes, int start, Module module) {
    offset = 1;
    if (bytes[start] == 0x40) {
      return new FunctionType();
    }

    WasmType maybeType = WasmType.typeMap.get(bytes[start]);

    if (maybeType == null) {
      return module.TypeSection.get(readInt(bytes, start)); // Offset is changed in readint
    } else {
      return new FunctionType(maybeType);
    }
  }

  // TODO: this
  public static void printWarning(String str) {
    System.out.println(str);
  }

  public static WasmType readTypeAnnotation(byte[] bytes, int start, WasmType allowedTypes) throws WasmParseError {
    byte code = bytes[start];
    offset = 1;
    switch (allowedTypes) {
      case any:
      return WasmType.typeMap.get(code);

      case numtype:
      return WasmType.numMap.get(code);

      case reftype:
      return WasmType.refMap.get(code);

      default:
      throw new WasmParseError(allowedTypes + " isn't a type that can be used as a set of allowed types");
    }
  }

  private static LinkedList<BlockType> blockTypeStack = new LinkedList<BlockType>();

  public static Expression readExpr(byte[] bytes, int index, Module module, WasmType[] locals) throws WasmParseError {
    List<Instruction> instructions = new LinkedList<Instruction>();
    List<Expression> blocks = new LinkedList<Expression>();

    Expression expr = new Expression(new Instruction[0], blocks);
    expr.localTypes = locals;

    int originalStart = index;

    while (bytes[index] != 0x0B && bytes[index] != 0x05) {
      FunctionType blockType;
      Expression block;
      int branchDepth;
      FunctionType newType;
      WasmType[] outputs;
      int v;
      index++;
      switch (bytes[index - 1]) {
        case 0x02:
        blockType = readBlockType(bytes, index, module);
        index += offset;
        blockTypeStack.addFirst(new BlockType(blockType));
        block = readExpr(bytes, index, module, locals);
        blockTypeStack.pollFirst();
        block.type = blockType;
        blocks.add(block);
        index += offset;
        instructions.add(new Instruction(new InstructionType(expr::enterBlock, blockType, new WasmType[] {WasmType.i32}, true, false), new Value[] {new ValueI32(blocks.size() - 1)}));
        break;

        case 0x03:
        blockType = readBlockType(bytes, index, module);
        index += offset;
        blockTypeStack.addFirst(new BlockType(blockType, true));
        block = readExpr(bytes, index, module, locals);
        blockTypeStack.pollFirst();
        block.type = blockType;
        block.isLoop = true;
        index += offset;
        instructions.add(new Instruction(new InstructionType(expr::enterBlock, blockType, new WasmType[] {WasmType.i32}, true, false), new Value[] {new ValueI32(blocks.size())}));
        blocks.add(block);
        break;

        case 0x04:
        blockType = readBlockType(bytes, index, module);
        index += offset;
        blockTypeStack.addFirst(new BlockType(blockType));
        block = readExpr(bytes, index, module, locals);
        block.type = blockType;
        int ifIndex = blocks.size();
        blocks.add(block);
        index += offset;

        int elseIndex = -1;
        
        if (bytes[index - 1] == 0x05) {
          block = readExpr(bytes, index, module, locals);
          block.type = blockType;
          elseIndex = blocks.size();
          blocks.add(block);
          index += offset;
        }

        blockTypeStack.pollFirst();

        WasmType[] newInput = new WasmType[blockType.inputs.length + 1];
        System.arraycopy(blockType.inputs, 0, newInput, 0, blockType.inputs.length);
        newInput[blockType.inputs.length] = WasmType.i32;
        newType = new FunctionType(newInput, blockType.outputs);

        if (elseIndex == -1) {
          instructions.add(new Instruction(new InstructionType(expr::enterBlockIf, newType, new WasmType[] {WasmType.i32}, true, false), new Value[] {new ValueI32(ifIndex)}));
        } else {
          instructions.add(new Instruction(new InstructionType(expr::enterBlockIfElse, newType, new WasmType[] {WasmType.i32, WasmType.i32}, true, false), new Value[] {new ValueI32(ifIndex), new ValueI32(elseIndex)}));
        }
        break;

        case 0x0C:
        branchDepth = readInt(bytes, index);
        index += offset;

        newType = new FunctionType();
        outputs = blockTypeStack.get(branchDepth).getResultType();
        newType.inputs = new WasmType[outputs.length];
        System.arraycopy(outputs, 0, newType.inputs, 0, newType.inputs.length);

        instructions.add(new Instruction(new InstructionType(ControlFlow::branch, newType, new WasmType[] {WasmType.i32}, false, true), new Value[] {new ValueI32(branchDepth)}));
        break;

        case 0x0D:
        branchDepth = readInt(bytes, index);
        index += offset;
        newType = new FunctionType();
        outputs = blockTypeStack.get(branchDepth).getResultType();
        newType.inputs = new WasmType[outputs.length + 1];
        System.arraycopy(outputs, 0, newType.inputs, 0, newType.inputs.length - 1);
        newType.inputs[newType.inputs.length - 1] = WasmType.i32;

        newType.outputs = outputs;

        instructions.add(new Instruction(new InstructionType(ControlFlow::branchIf, newType, new WasmType[] {WasmType.i32}), new Value[] {new ValueI32(branchDepth)}));
        break;

        case 0x0E:
        int labelIndexAmt = readInt(bytes, index) + 1; // +1 to capture the default value
        index += offset;
        Integer[] labelIndexes = new Integer[labelIndexAmt];
        int len = blockTypeStack.size();
        for (int i = 0; i < labelIndexAmt; i++) {
          labelIndexes[i] = readInt(bytes, index);
          index += offset;
          if (labelIndexes[i] >= len) {
            throw new WasmParseError("Error parsing br_table instruction, there's no block with index " + labelIndexes[i]);
          }
        }

        WasmType[] resultType = blockTypeStack.get(labelIndexes[labelIndexAmt - 1]).getResultType();
        newType = new FunctionType(new WasmType[resultType.length + 1], new WasmType[0]);
        System.arraycopy(resultType, 0, newType.inputs, 0, newType.inputs.length - 1);
        newType.inputs[newType.inputs.length - 1] = WasmType.i32;

        System.out.println(newType);

        instructions.add(new Instruction(new InstructionType(ControlFlow::branchTable, newType, new WasmType[] {}, false, true), Arrays.stream(labelIndexes).map(ValueI32::fromInt).toArray(Value[]::new)));
        break;

        case 0x0F:
        newType = new FunctionType();
        outputs = blockTypeStack.getLast().getResultType();
        newType.inputs = new WasmType[outputs.length];
        System.arraycopy(outputs, 0, newType.inputs, 0, newType.inputs.length);
        newType.outputs = newType.inputs;

        instructions.add(new Instruction(new InstructionType(ControlFlow::branch, newType, new WasmType[] {WasmType.i32}, false, true), new Value[] {new ValueI32(blockTypeStack.size() - 1)}));
        break;

        case 0x10:
        int funcIndex = readInt(bytes, index);
        index += offset;

        if (module.Functions.size() <= funcIndex) throw new WasmParseError("There's no function at position " + funcIndex);

        instructions.add(new Instruction(new InstructionType(Functions::call, module.Functions.get(funcIndex).type, new WasmType[] {WasmType.i32}), new Value[] {new ValueI32(funcIndex)}));
        break;

        case 0x11:
        if (module.Tables.size() == 0) throw new WasmParseError("Can't use call_indirect, there are no tables defined");
        
        int typeIndex = readInt(bytes, index);
        index += offset;

        int tableIndex = readInt(bytes, index);
        index += offset;

        if (module.Tables.get(tableIndex).type != WasmType.funcref) throw new WasmParseError("Can't use call_indirect, table 0 is not of type funcref");

        FunctionType oldType = module.TypeSection.get(typeIndex);
        newType = new FunctionType(new WasmType[oldType.inputs.length + 1], oldType.outputs);
        System.arraycopy(oldType.inputs, 0, newType.inputs, 0, oldType.inputs.length);
        newType.inputs[newType.inputs.length - 1] = WasmType.i32;

        instructions.add(new Instruction(new InstructionType(Functions::callIndirect, newType, new WasmType[] {WasmType.i32, WasmType.i32}), new Value[] {new ValueI32(typeIndex), new ValueI32(tableIndex)}));
        break;

        case 0x20:
        v = readInt(bytes, index);
        index += offset;

        instructions.add(new Instruction(new InstructionType(expr::localGet, Opcodes.get, new WasmType[] {WasmType.i32}, GenericTypeRequirers.local), new Value[] {new ValueI32(v)}));
        break;

        case 0x21:
        v = readInt(bytes, index);
        index += offset;

        instructions.add(new Instruction(new InstructionType(expr::localSet, Opcodes.set, new WasmType[] {WasmType.i32}, GenericTypeRequirers.local), new Value[] {new ValueI32(v)}));
        break;

        case 0x22:
        v = readInt(bytes, index);
        index += offset;
        
        instructions.add(new Instruction(new InstructionType(expr::localTee, Opcodes.tee, new WasmType[] {WasmType.i32}, GenericTypeRequirers.local), new Value[] {new ValueI32(v)}));
        break;

        default:
        InstructionType type;

        switch (bytes[index - 1]) {
          case (byte) 0xFC:
          type = Opcodes.opcodeExtendedMap.get(bytes[index]);
          index++;
          break;

          default:
          type = Opcodes.opcodeMap.get(bytes[index - 1]);
        }

        WasmType typeAnnotation = null;

        if (type.allowedAnnotations != null) {
          typeAnnotation = readTypeAnnotation(bytes, index, type.allowedAnnotations);
        }

        Value[] immediates = new Value[type.immediates.length];

        for (int i = 0; i < immediates.length; i++) {
          switch (type.immediates[i]) {
            case i32:
            immediates[i] = new ValueI32(readInt(bytes, index));
            break;

            case i64:
            immediates[i] = new ValueI64(readLong(bytes, index));
            break;

            case f32:
            immediates[i] = new ValueF32(readFloat(bytes, index));
            break;

            case f64:
            immediates[i] = new ValueF64(readDouble(bytes, index));
            break;

            default:
            throw new WasmParseError("Can't use type " + type.immediates[i] + "as an immediate");
          }

          index += offset;
        }

        instructions.add(new Instruction(type, immediates, typeAnnotation));
      }
    }

    expr.bytecode = instructions.toArray(new Instruction[0]);
    offset = index - originalStart + 1;

    return expr;
  }
}
