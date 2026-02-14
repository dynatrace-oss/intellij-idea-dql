package pl.thedeem.intellij.common.sdk.errors;

import pl.thedeem.intellij.common.sdk.model.DQLErrorResponse;

import java.util.Map;
import java.util.Objects;

public abstract class DQLDetailedErrorException extends DQLInvalidResponseException {
    protected final DQLErrorResponse response;

    protected DQLDetailedErrorException(String message, DQLErrorResponse response) {
        super(message);
        this.response = response;
    }

    public DQLErrorResponse getResponse() {
        return response;
    }

    public Map<String, Object> getErrorDetails() {
        return response != null && response.error != null ?
                Objects.requireNonNullElse(response.error.details(), Map.of())
                : Map.of();
    }

    protected String getResponseMessage() {
        if (response != null) {
            DQLErrorResponse.Error reason = response.error;
            if (reason != null) {
                if (reason.details() == null || !(reason.details().get("errorMessage") instanceof String str)) {
                    return reason.message();
                }
                return str;
            }
        }
        return null;
    }

}
