package pl.thedeem.intellij.dql.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
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
