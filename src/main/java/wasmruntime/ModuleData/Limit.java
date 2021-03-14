package wasmruntime.ModuleData;

public class Limit {
  public int min = 0;
  public int max = 0;

  public Limit(int minOof, int maxOof) {
    min = minOof;
    max = maxOof;
  }

  public boolean IsValid(int range) {
    if (max > range) return false;

    if (max != -1) {
      return min <= max;
    }

    return min <= range;
  }

  public boolean withinLimits(int value) {
    return min <= value && (max >= value || max == -1);
  }

  public String toString() {
    return min+" - "+max;
  }
}
