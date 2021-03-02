package wasmruntime.Errors;

public class WasmValidationError extends Exception {
  private static final long serialVersionUID = 7718828512143293558L;
  
  public WasmValidationError(String errorMsg) {
    super(errorMsg);
  }
}
