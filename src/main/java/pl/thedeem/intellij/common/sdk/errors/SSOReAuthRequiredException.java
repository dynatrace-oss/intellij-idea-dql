package pl.thedeem.intellij.common.sdk.errors;

public class SSOReAuthRequiredException extends SSOAuthException {
    public SSOReAuthRequiredException(String message) {
        super(message);
    }
}
