package pl.thedeem.intellij.dql.sdk.errors;

import pl.thedeem.intellij.dql.sdk.model.errors.DQLAuthErrorResponse;
import pl.thedeem.intellij.dql.sdk.model.errors.DQLErrorResponse;

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
