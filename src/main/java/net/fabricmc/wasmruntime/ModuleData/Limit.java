package net.fabricmc.wasmruntime.ModuleData;

public class Limit {
  public int min = 0;
  public int max = 0;

  public Limit(int minOof, int maxOof) {
    min = minOof;
    max = maxOof;
  }

  public String toString() {
    return min+" - "+max;
  }
}
