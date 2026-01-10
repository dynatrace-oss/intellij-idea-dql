package pl.thedeem.intellij.common.sdk.errors;

import pl.thedeem.intellij.common.sdk.model.errors.DQLAuthErrorResponse;
import pl.thedeem.intellij.common.sdk.model.errors.DQLErrorResponse;

public class DQLNotAuthorizedException extends DQLInvalidResponseException {
    private final DQLErrorResponse<DQLAuthErrorResponse> response;

    public DQLNotAuthorizedException(String message, DQLErrorResponse<DQLAuthErrorResponse> response) {
        super(message);
        this.response = response;
    }

    public DQLErrorResponse<DQLAuthErrorResponse> getResponse() {
        return response;
    }

    @Override
    protected String getResponseMessage() {
        DQLErrorResponse<DQLAuthErrorResponse> response = getResponse();
        if (response != null) {
            DQLAuthErrorResponse reason = response.error;
            if (reason != null && reason.details != null) {
                return reason.details.get("errorMessage") instanceof String str ? str : null;
            }
        }
        return null;
    }
}
