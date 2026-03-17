package pl.thedeem.intellij.common.sdk.errors;

public class SSOAuthException extends DTAuthException {
    public SSOAuthException(String message) {
        super(message);
    }

    public SSOAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
