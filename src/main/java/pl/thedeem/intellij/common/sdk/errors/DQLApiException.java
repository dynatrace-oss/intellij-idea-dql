package pl.thedeem.intellij.common.sdk.errors;

import java.io.IOException;

public class DQLApiException extends IOException {
    public DQLApiException(String message) {
        super(message);
    }
}
