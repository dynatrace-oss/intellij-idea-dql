package pl.thedeem.intellij.common.sdk.errors;

public class DQLResponseParsingException extends DQLApiException {
    private final String response;

    public DQLResponseParsingException(String message, String response) {
        super(message);
        this.response = response;
    }

    public String getResponse() {
        return response;
    }
}
