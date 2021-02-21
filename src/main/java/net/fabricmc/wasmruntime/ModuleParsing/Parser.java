package net.fabricmc.wasmruntime.ModuleParsing;

import net.fabricmc.wasmruntime.ModuleData.FunctionType;
import net.fabricmc.wasmruntime.ModuleData.Module;
import net.fabricmc.wasmruntime.ModuleData.WasmType;

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
        case 0:
        module.CustomSection.put(readName(bytes, index), Arrays.copyOfRange(bytes, index + offset, end));
        break;
        
        case 1:
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

        System.out.println(module.TypeSection);
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
}
