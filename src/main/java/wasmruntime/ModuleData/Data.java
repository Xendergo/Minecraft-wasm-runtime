package wasmruntime.ModuleData;

public class Data {
  public byte[] bytes;

  public Data(byte[] bytesOof) {
    bytes = bytesOof;
  }

  public void copyTo(byte[] toCopyIn, int targetOffset, int sourceOffset, int length) {
    System.arraycopy(bytes, sourceOffset, toCopyIn, targetOffset, length);
  }
}
