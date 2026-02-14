package pl.thedeem.intellij.common.sdk.errors;

import java.util.Objects;

public abstract class DQLInvalidResponseException extends DQLApiException {
    public DQLInvalidResponseException(String message) {
        super(message);
    }

    public String getApiMessage() {
        return Objects.requireNonNullElseGet(getResponseMessage(), this::getMessage);
    }

    protected abstract String getResponseMessage();
}
