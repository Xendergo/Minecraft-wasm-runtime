package net.fabricmc.wasmruntime.ModuleData.HelpfulEnums;

import java.util.HashMap;

public enum ElementType {
  funcref;

  public static HashMap<Byte, ElementType> elementTypeMap = new HashMap<Byte, ElementType>();

  static {
    elementTypeMap.put((byte) 0x70, funcref);
  }
}
