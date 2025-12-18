package pl.thedeem.intellij.common.sdk.errors;

import pl.thedeem.intellij.common.sdk.model.errors.DQLAuthErrorResponse;
import pl.thedeem.intellij.common.sdk.model.errors.DQLErrorResponse;

public class DQLNotAuthorizedException extends DQLApiException {
    private final DQLErrorResponse<DQLAuthErrorResponse> response;

    public DQLNotAuthorizedException(String message, DQLErrorResponse<DQLAuthErrorResponse> response) {
        super(message);
        this.response = response;
    }

    public DQLErrorResponse<DQLAuthErrorResponse> getResponse() {
        return response;
    }
}
