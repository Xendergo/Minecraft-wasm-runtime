package net.fabricmc.wasmruntime.ModuleParsing;

import net.fabricmc.wasmruntime.ModuleData.Code;
import net.fabricmc.wasmruntime.ModuleData.ConstantExpression;
import net.fabricmc.wasmruntime.ModuleData.Export;
import net.fabricmc.wasmruntime.ModuleData.Expression;
import net.fabricmc.wasmruntime.ModuleData.FunctionType;
import net.fabricmc.wasmruntime.ModuleData.Global;
import net.fabricmc.wasmruntime.ModuleData.ImportedFunction;
import net.fabricmc.wasmruntime.ModuleData.ImportedGlobal;
import net.fabricmc.wasmruntime.ModuleData.Limit;
import net.fabricmc.wasmruntime.ModuleData.Memory;
import net.fabricmc.wasmruntime.ModuleData.Module;
import net.fabricmc.wasmruntime.ModuleData.WasmFunction;
import net.fabricmc.wasmruntime.ModuleData.Table;
import net.fabricmc.wasmruntime.ModuleData.HelpfulEnums.ElementType;
import net.fabricmc.wasmruntime.ModuleData.HelpfulEnums.ExportTypes;
import net.fabricmc.wasmruntime.ModuleData.HelpfulEnums.WasmType;
import net.fabricmc.wasmruntime.ModuleExecutor.ExecExpression;
import net.fabricmc.wasmruntime.ModuleExecutor.Value;
import net.fabricmc.wasmruntime.ModuleExecutor.ValueF32;
import net.fabricmc.wasmruntime.ModuleExecutor.ValueF64;
import net.fabricmc.wasmruntime.ModuleExecutor.ValueI32;
import net.fabricmc.wasmruntime.ModuleExecutor.ValueI64;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
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
            
            int outputAmt = readInt(bytes, index);
            index += offset;

            WasmType[] in = new WasmType[inputAmt];
            WasmType[] out = new WasmType[outputAmt];
            
            for (int j = 0; j < inputAmt; j++) {
              in[j] = WasmType.typeMap.get(bytes[index]);
              index++;
            }

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
            module.Functions.add(new ImportedFunction(moduleName, importName, bytes[index]));
            index++;
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
              module.Globals.add(new ImportedGlobal<ValueI32>(moduleName, importName, mutable));
              break;

              case i64:
              module.Globals.add(new ImportedGlobal<ValueI64>(moduleName, importName, mutable));
              break;

              case f32:
              module.Globals.add(new ImportedGlobal<ValueF32>(moduleName, importName, mutable));
              break;

              case f64:
              module.Globals.add(new ImportedGlobal<ValueF64>(moduleName, importName, mutable));
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
          module.Functions.add(new WasmFunction(readInt(bytes, index)));
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
          ConstantExpression expr = new ConstantExpression(readExpr(bytes, index), type);
          index += offset;

          if (!expr.IsValid()) {
            throw new WasmParseError("Invalid global initializer with global index " + i);
          }
          Value ret = ExecExpression.Exec(expr).stack[0];

          switch (type) {
            case i32:
            module.Globals.add(new Global<ValueI32>((ValueI32)ret, mutable));
            break;

            case i64:
            module.Globals.add(new Global<ValueI64>((ValueI64)ret, mutable));
            break;

            case f32:
            module.Globals.add(new Global<ValueF32>((ValueF32)ret, mutable));
            break;

            case f64:
            module.Globals.add(new Global<ValueF64>((ValueF64)ret, mutable));
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

          ConstantExpression expr = new ConstantExpression(readExpr(bytes, index), WasmType.i32);
          index += offset;

          if (!expr.IsValid()) {
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

          Expression expr = new Expression(readExpr(bytes, index));
          index += offset;

          module.Codes.add(new Code(locals, expr));
        }
        break;

        case 11: // Data section https://webassembly.github.io/spec/core/binary/modules.html#data-section
        int dataAmt = readInt(bytes, index);
        index += offset;

        for (int i = 0; i < dataAmt; i++) {
          int memoryIndex = readInt(bytes, index);
          index += offset;

          ConstantExpression expr = new ConstantExpression(readExpr(bytes, index), WasmType.i32);
          index += offset;

          if (!expr.IsValid()) {
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

  /*
  TODO: Make this skip immediate values
  */
  public static byte[] readExpr(byte[] bytes, int start) {
    int blockDepth = 0;

    int originalStart = start;

    while (blockDepth != 0 || bytes[start] != 0x0B) {
      if (bytes[start] == 0x0B) {
        blockDepth--;
      }

      if (bytes[start] > 0x01 && bytes[start] < 0x05) {
        blockDepth++;
      }

      start++;
    }

    offset = start - originalStart + 1;

    return Arrays.copyOfRange(bytes, originalStart, start);
  }
}
