package net.fabricmc.wasmruntime.ModuleData;

import java.util.HashMap;

public enum WasmType {
  i32,
  i64,
  f32,
  f64;

  public static HashMap<Byte, WasmType> typeMap = new HashMap<Byte, WasmType>();

  static {
    typeMap.put((byte) 0x7F, i32);
    typeMap.put((byte) 0x7E, i64);
    typeMap.put((byte) 0x7D, f32);
    typeMap.put((byte) 0x7C, f64);
  }
}
