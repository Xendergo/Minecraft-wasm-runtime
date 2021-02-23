package net.fabricmc.wasmruntime.ModuleData.HelpfulEnums;

import java.util.HashMap;

public enum ExportTypes {
  Func,
  Table,
  Memory,
  Global;

  public static HashMap<Byte, ExportTypes> ExportTypeMap = new HashMap<Byte, ExportTypes>();
  static {
    ExportTypeMap.put((byte) 0x00, Func);
    ExportTypeMap.put((byte) 0x01, Table);
    ExportTypeMap.put((byte) 0x02, Memory);
    ExportTypeMap.put((byte) 0x03, Global);
  }
}
