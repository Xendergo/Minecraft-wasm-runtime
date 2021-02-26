package net.fabricmc.wasmruntime.ModuleParsing;

import net.fabricmc.wasmruntime.ModuleData.Code;
import net.fabricmc.wasmruntime.ModuleData.Export;
import net.fabricmc.wasmruntime.ModuleData.Expression;
import net.fabricmc.wasmruntime.ModuleData.FunctionType;
import net.fabricmc.wasmruntime.ModuleData.Global;
import net.fabricmc.wasmruntime.ModuleData.ImportedFunction;
import net.fabricmc.wasmruntime.ModuleData.ImportedGlobal;
import net.fabricmc.wasmruntime.ModuleData.Limit;
import net.fabricmc.wasmruntime.ModuleData.Memory;
import net.fabricmc.wasmruntime.ModuleData.Module;
import net.fabricmc.wasmruntime.ModuleData.Opcodes;
import net.fabricmc.wasmruntime.ModuleData.Table;
import net.fabricmc.wasmruntime.ModuleData.WasmFunction;
import net.fabricmc.wasmruntime.ModuleData.HelpfulEnums.ElementType;
import net.fabricmc.wasmruntime.ModuleData.HelpfulEnums.ExportTypes;
import net.fabricmc.wasmruntime.ModuleData.HelpfulEnums.WasmType;
import net.fabricmc.wasmruntime.ModuleExecutor.ExecExpression;
import net.fabricmc.wasmruntime.ModuleExecutor.Instruction;
import net.fabricmc.wasmruntime.ModuleExecutor.InstructionType;
import net.fabricmc.wasmruntime.ModuleExecutor.Value;
import net.fabricmc.wasmruntime.ModuleExecutor.ValueF32;
import net.fabricmc.wasmruntime.ModuleExecutor.ValueF64;
import net.fabricmc.wasmruntime.ModuleExecutor.ValueI32;
import net.fabricmc.wasmruntime.ModuleExecutor.ValueI64;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.fabricmc.wasmruntime.Errors.WasmParseError;

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

            switch (type) {
              case i32:
              module.Globals.add(new ImportedGlobal<ValueI32>(moduleName, importName, mutable, WasmType.i32));
              break;

              case i64:
              module.Globals.add(new ImportedGlobal<ValueI64>(moduleName, importName, mutable, WasmType.i64));
              break;

              case f32:
              module.Globals.add(new ImportedGlobal<ValueF32>(moduleName, importName, mutable, WasmType.f32));
              break;

              case f64:
              module.Globals.add(new ImportedGlobal<ValueF64>(moduleName, importName, mutable, WasmType.f64));
              break;

              default:
              throw new WasmParseError("Mystery type " + type + " being imported as a global");
            }
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
          ElementType type = ElementType.elementTypeMap.get(bytes[index]);
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
          Expression expr = readExpr(bytes, index);
          expr.type = new FunctionType(type);
          index += offset;

          if (!expr.IsValid(true, module.Globals.toArray(new Global[0]))) {
            throw new WasmParseError("Invalid global initializer with global index " + i);
          }
          Value ret = ExecExpression.Exec(expr).stack[0];

          switch (type) {
            case i32:
            module.Globals.add(new Global<ValueI32>((ValueI32)ret, mutable, WasmType.i32));
            break;

            case i64:
            module.Globals.add(new Global<ValueI64>((ValueI64)ret, mutable, WasmType.i64));
            break;

            case f32:
            module.Globals.add(new Global<ValueF32>((ValueF32)ret, mutable, WasmType.f32));
            break;

            case f64:
            module.Globals.add(new Global<ValueF64>((ValueF64)ret, mutable, WasmType.f64));
            break;

            default:
            throw new WasmParseError("Mystery type " + type + " being used as a global");
          }
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

          Expression expr = readExpr(bytes, index);
          expr.type = new FunctionType(WasmType.i32);
          index += offset;

          if (!expr.IsValid(true, module.Globals.toArray(new Global[0]))) {
            throw new WasmParseError("Constant expression for offset of values in table "+tableIndex+" is invalid");
          }

          int tableOffset = ((ValueI32)ExecExpression.Exec(expr).stack[0]).value;

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

          Expression expr = readExpr(bytes, index);
          index += offset;
          expr.type = module.TypeSection.get(module.FunctionTypeIndices.get(module.Codes.size()));

          Code code = new Code(locals, expr);

          module.Functions.add(new WasmFunction(code));

          module.Codes.add(code);
        }
        break;

        case 11: // Data section https://webassembly.github.io/spec/core/binary/modules.html#data-section
        int dataAmt = readInt(bytes, index);
        index += offset;

        for (int i = 0; i < dataAmt; i++) {
          int memoryIndex = readInt(bytes, index);
          index += offset;

          Expression expr = readExpr(bytes, index);
          expr.type = new FunctionType(WasmType.i32);
          index += offset;

          if (!expr.IsValid(true, module.Globals.toArray(new Global[0]))) {
            throw new WasmParseError("Constant expression for offset of data index " + i + "is invalid");
          }

          int memoryOffset = ((ValueI32)ExecExpression.Exec(expr).stack[0]).value;

          int byteAmt = readInt(bytes, index);
          index += offset;

          byte[] memory = module.Memories.get(memoryIndex).data;
          byte[] data = Arrays.copyOfRange(bytes, index, index + byteAmt);

          try {
            for (int j = 0; j < byteAmt; j++) {
              memory[j + memoryOffset] = data[j];
            }
          } catch (ArrayIndexOutOfBoundsException e) {
            throw new WasmParseError("Not enough memory to be able to store the data at index " + memoryOffset);
          }
        }
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

  public static void printWarning(String str) {
    System.out.println(str);
  }

  public static Expression readExpr(byte[] bytes, int index) throws WasmParseError {
    List<Instruction> instructions = new LinkedList<Instruction>();
    List<Expression> blocks = new LinkedList<Expression>();

    int originalStart = index;

    while (bytes[index] != 0x0B && bytes[index] != 0x05) {
      switch (bytes[index]) {
        case 0x02:
        index++;
        blocks.add(readExpr(bytes, index));
        index += offset;
        // TODO: Go into the block when reached
        break;

        case 0x03:
        index++;
        blocks.add(readExpr(bytes, index));
        index += offset;
        // TODO: Go to block when reached and loop when execution ends
        break;

        case 0x04:
        index++;
        blocks.add(readExpr(bytes, index));
        index += offset;

        if (bytes[index - 1] == 0x05) {
          blocks.add(readExpr(bytes, index));
          index += offset;
          // TODO: Go to else block if condition is false
        }
        // TODO: Go to block when reached if condition is true
        break;

        case 0x0C:
        // TODO: Break instruction
        break;

        case 0x0D:
        // TODO: Break if instruction
        break;

        case 0x0E:
        // TODO: Break table instruction
        break;

        case 0x0F:
        // TODO: Return instruction
        break;

        case 0x10:
        // TODO: Call instruction
        break;

        case 0x11:
        // TODO: Call indirect instruction
        break;

        default:
        InstructionType type;

        if (bytes[index] == 0xFC) {
          type = Opcodes.truncMap.get(bytes[index + 1]);
          index += 2;
        } else {
          type = Opcodes.opcodeMap.get(bytes[index]);
          index++;
        }

        List<Value> immediates = new ArrayList<Value>();

        for (WasmType wasmType : type.immediates) {
          switch (wasmType) {
            case i32:
            immediates.add(new ValueI32(readInt(bytes, index)));
            break;

            case i64:
            immediates.add(new ValueI64(readLong(bytes, index)));
            break;

            case f32:
            immediates.add(new ValueF32(readFloat(bytes, index)));
            break;

            case f64:
            immediates.add(new ValueF64(readDouble(bytes, index)));
            break;

            default:
            throw new WasmParseError("Can't use type " + wasmType + "as an immediate");
          }

          index += offset;
        }

        instructions.add(new Instruction(type, immediates));
      }
    }

    offset = index - originalStart + 1;

    return new Expression(instructions.toArray(new Instruction[0]), blocks);
  }
}
