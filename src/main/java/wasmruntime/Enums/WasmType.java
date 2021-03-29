package wasmruntime.Enums;

import java.util.HashMap;
import java.util.Map;

public enum WasmType {
  I32,
  I64,
  F32,
  F64,
  V128,
  Funcref,
  Externref;

  public static Map<Byte, WasmType> idMap = new HashMap<Byte, WasmType>();

  static {
    idMap.put((byte) 0, I32);
    idMap.put((byte) 1, I64);
    idMap.put((byte) 2, F32);
    idMap.put((byte) 3, F64);
    idMap.put((byte) 4, V128);
    idMap.put((byte) 5, Funcref);
    idMap.put((byte) 6, Externref);
  }
}