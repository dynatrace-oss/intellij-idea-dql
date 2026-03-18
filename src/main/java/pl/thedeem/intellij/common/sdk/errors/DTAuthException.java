package pl.thedeem.intellij.common.sdk.errors;

public class DTAuthException extends DTIoException {
    public DTAuthException(String message) {
        super(message);
    }

    public DTAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
