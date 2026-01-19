package pl.thedeem.intellij.common.sdk.errors;

import pl.thedeem.intellij.common.sdk.model.DQLErrorResponse;

public class DQLErrorResponseException extends DQLDetailedErrorException {
    public DQLErrorResponseException(String message, DQLErrorResponse response) {
        super(message, response);
    }
}
