package net.fabricmc.wasmruntime.ModuleData;

public class Memory {
  Limit limit;
  byte[] data;

  public Memory(Limit limitOof) {
    limit = limitOof;

    data = new byte[limit.min];
  }

  public String toString() {
    return "Memory {"+limit + ", length: " + data.length + "}";
  }
}
