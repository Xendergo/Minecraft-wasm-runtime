package wasmruntime.Errors;

public class TrapRuntime extends RuntimeException {
  private static final long serialVersionUID = 7718828512143293558L;
  
  public TrapRuntime(String errorMsg) {
    super(errorMsg);
  }
}
