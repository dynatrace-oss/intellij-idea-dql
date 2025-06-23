package pl.thedeem.intellij.dql.sdk.model;

public class DQLExecuteResponse {
    String requestToken;
    String state;
    Integer ttlSeconds;

    public String getRequestToken() {
        return requestToken;
    }

    public String getState() {
        return state;
    }

    public Integer getTtlSeconds() {
        return ttlSeconds;
    }
}
