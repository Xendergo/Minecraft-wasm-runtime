package wasmruntime.Exceptions;

public class WasmtimeEmbeddingException extends Exception {
    private static final long serialVersionUID = 1933891963804229764L;

    public WasmtimeEmbeddingException(String err) {
        super(err);
    }
}
