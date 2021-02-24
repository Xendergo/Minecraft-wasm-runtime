package net.fabricmc.wasmruntime.ModuleData;

public class Memory {
  public Limit limit;
  public byte[] data;

  public Memory(Limit limitOof) {
    limit = limitOof;

    data = new byte[limit.min << 16];
  }

  public boolean IsValid() {
    return limit.IsValid(65535);
  }

  public String toString() {
    String dataString = "";
    for (int i = 0; i < data.length; i++) {
      dataString += data[i] + ", ";
    }
    return "Memory {"+limit + ", pages: " + (data.length >>> 16) + ", data: [" + dataString + "]}";
  }
}
