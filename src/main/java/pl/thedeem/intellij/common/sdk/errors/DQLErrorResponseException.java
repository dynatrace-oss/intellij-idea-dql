package pl.thedeem.intellij.common.sdk.errors;

import pl.thedeem.intellij.common.sdk.model.errors.DQLErrorResponse;
import pl.thedeem.intellij.common.sdk.model.errors.DQLExecutionErrorResponse;

public class DQLErrorResponseException extends DQLApiException {
    private final DQLErrorResponse<DQLExecutionErrorResponse> response;

    public DQLErrorResponseException(String message, DQLErrorResponse<DQLExecutionErrorResponse> response) {
        super(message);
        this.response = response;
    }

    public DQLErrorResponse<DQLExecutionErrorResponse> getResponse() {
        return response;
    }
}
