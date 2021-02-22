package net.fabricmc.wasmruntime.ModuleParsing;

import net.fabricmc.wasmruntime.ModuleData.ExportedFunction;
import net.fabricmc.wasmruntime.ModuleData.FunctionType;
import net.fabricmc.wasmruntime.ModuleData.ImportedFunction;
import net.fabricmc.wasmruntime.ModuleData.Limit;
import net.fabricmc.wasmruntime.ModuleData.Memory;
import net.fabricmc.wasmruntime.ModuleData.Module;
import net.fabricmc.wasmruntime.ModuleData.WasmFunction;
import net.fabricmc.wasmruntime.ModuleData.WasmTable;
import net.fabricmc.wasmruntime.ModuleData.HelpfulEnums.ElementType;
import net.fabricmc.wasmruntime.ModuleData.HelpfulEnums.WasmType;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

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

            ArrayList<WasmType> in = new ArrayList<WasmType>();
            ArrayList<WasmType> out = new ArrayList<WasmType>();

            int inputAmt = readInt(bytes, index);
            index += offset;

            for (int j = 0; j < inputAmt; j++) {
              in.add(WasmType.typeMap.get(bytes[index]));
              index++;
            }

            int outputAmt = readInt(bytes, index);
            index += offset;

            for (int j = 0; j < outputAmt; j++) {
              out.add(WasmType.typeMap.get(bytes[index]));
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
            module.Functions.add(new ImportedFunction(moduleName, importName, bytes[index + 1]));
            break;

            case 0x01:
            throw new WasmParseError("Importing tables is unsupported");

            case 0x02:
            throw new WasmParseError("Importing memory is unsupported");

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
          module.Tables.add(new WasmTable(limit, type));
        }
        break;

        case 5: // Memory section https://webassembly.github.io/spec/core/binary/modules.html#memory-section
        int memoryAmt = readInt(bytes, index);
        index += offset;

        for (int i = 0; i < memoryAmt; i++) {
          module.Memories.add(new Memory(readLimit(bytes, index)));
        }
        break;

        case 7:
        int exportAmt = readInt(bytes, index);
        index += offset;

        for (int i = 0; i < exportAmt; i++) {
          String name = readName(bytes, index);
          index += offset;

          switch (bytes[index]) {
            case 0x00:
            index++;
            module.exportedFunctions.add(new ExportedFunction(name, readInt(bytes, index)));
            break;

            case 0x01:
            printWarning("Exporting tables is unneccesary");
            break;

            case 0x02:
            if (module.ExportedMemory != -1) {
              throw new WasmParseError("Can't export more than one memory");
            }
            index++;
            module.ExportedMemory = readInt(bytes, index);
            break;
          }

          index++;
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
}
