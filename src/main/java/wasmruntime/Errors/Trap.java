package wasmruntime.Errors;

public class Trap extends Exception {
  private static final long serialVersionUID = 7718828512143293558L;
  
  public Trap(String errorMsg) {
    super(errorMsg);
  }
}
