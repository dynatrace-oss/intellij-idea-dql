package pl.thedeem.intellij.common.sdk.errors;

import java.util.Objects;

public abstract class DQLInvalidResponseException extends DTIoException {
    public DQLInvalidResponseException(String message) {
        super(message);
    }

    public String getApiMessage() {
        return Objects.requireNonNullElseGet(getResponseMessage(), this::getMessage);
    }

    protected abstract String getResponseMessage();
}
