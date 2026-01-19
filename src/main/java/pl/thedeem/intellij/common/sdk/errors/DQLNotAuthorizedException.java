package pl.thedeem.intellij.common.sdk.errors;

import pl.thedeem.intellij.common.sdk.model.DQLErrorResponse;

public class DQLNotAuthorizedException extends DQLDetailedErrorException {
    public DQLNotAuthorizedException(String message, DQLErrorResponse response) {
        super(message, response);
    }
}
