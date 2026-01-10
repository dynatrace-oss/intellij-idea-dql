package pl.thedeem.intellij.common.sdk.errors;

public class DQLResponseRedirectedException extends DQLInvalidResponseException {
    private final String redirectionUrl;

    public DQLResponseRedirectedException(String message, String redirectionUrl) {
        super(message);
        this.redirectionUrl = redirectionUrl;
    }

    public String getRedirectionUrl() {
        return redirectionUrl;
    }

    @Override
    protected String getResponseMessage() {
        return "The request was redirected to " + redirectionUrl;
    }
}
