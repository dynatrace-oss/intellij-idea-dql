package pl.thedeem.intellij.dql.sdk.errors;

public class DQLResponseRedirectedException extends DQLApiException {
    private final String redirectionUrl;

    public DQLResponseRedirectedException(String message, String redirectionUrl) {
        super(message);
        this.redirectionUrl = redirectionUrl;
    }

    public String getRedirectionUrl() {
        return redirectionUrl;
    }
}
