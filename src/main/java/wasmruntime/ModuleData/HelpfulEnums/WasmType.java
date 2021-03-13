package wasmruntime.ModuleData.HelpfulEnums;

import java.util.HashMap;
import java.util.HashSet;

public enum WasmType {
  i32,
  i64,
  f32,
  f64,
  funcref,
  externref,
  numtype,
  reftype,
  any,
  T;

  public static HashMap<Byte, WasmType> typeMap = new HashMap<Byte, WasmType>();
  public static HashMap<Byte, WasmType> numMap = new HashMap<Byte, WasmType>();
  public static HashMap<Byte, WasmType> refMap = new HashMap<Byte, WasmType>();

  public static HashSet<WasmType> numtypeSet = new HashSet<WasmType>();
  public static HashSet<WasmType> reftypeSet = new HashSet<WasmType>();

  public static HashMap<Byte, WasmType> elemKindMap = new HashMap<Byte, WasmType>();

  public static boolean equal(WasmType a, WasmType b) {
    if (a == b) return true;
    if (a == any || b == any) return true;
    if (a == numtype && numtypeSet.contains(b)) return true;
    if (a == reftype && reftypeSet.contains(b)) return true;
    if (b == numtype && numtypeSet.contains(a)) return true;
    if (b == reftype && reftypeSet.contains(a)) return true;

    return false;
  }

  static {
    typeMap.put((byte) 0x7F, i32);
    typeMap.put((byte) 0x7E, i64);
    typeMap.put((byte) 0x7D, f32);
    typeMap.put((byte) 0x7C, f64);
    typeMap.put((byte) 0x70, funcref);
    typeMap.put((byte) 0x6f, externref);

    numMap.put((byte) 0x7F, i32);
    numMap.put((byte) 0x7E, i64);
    numMap.put((byte) 0x7D, f32);
    numMap.put((byte) 0x7C, f64);
    refMap.put((byte) 0x70, funcref);
    refMap.put((byte) 0x6f, externref);

    elemKindMap.put((byte) 0x00, funcref);

    numtypeSet.add(i32);
    numtypeSet.add(i64);
    numtypeSet.add(f32);
    numtypeSet.add(f64);
    reftypeSet.add(funcref);
    reftypeSet.add(externref);
  }
}
