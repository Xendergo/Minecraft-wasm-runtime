package wasmruntime.ModuleData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Memory {
  public Limit limit;
  public ByteBuffer data;

  public Memory(Limit limitOof) {
    limit = limitOof;

    data = ByteBuffer.wrap(new byte[limit.min << 16]);
    data.order(ByteOrder.LITTLE_ENDIAN);
  }

  public boolean IsValid() {
    return limit.IsValid(65535);
  }

  public String toString() {
    String dataString = "";
    byte[] bytes = data.array();
    for (int i = 0; i < bytes.length && i < 128; i++) {
      dataString += bytes[i] + ", ";
    }
    dataString += "...";
    return "Memory {" + limit + ", pages: " + (bytes.length >>> 16) + ", data: [" + dataString + "]}";
  }
}
