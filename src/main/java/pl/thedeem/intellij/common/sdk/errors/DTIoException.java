package pl.thedeem.intellij.common.sdk.errors;

import java.io.IOException;

public class DTIoException extends IOException {
    public DTIoException(String message) {
        super(message);
    }

    public DTIoException(String message, Throwable cause) {
        super(message, cause);
    }
}
