package net.fabricmc.wasmruntime.Errors;

public class WasmParseError extends Exception {
  private static final long serialVersionUID = 7718828512143293558L;
  
  public WasmParseError(String errorMsg) {
    super(errorMsg);
  }
}
