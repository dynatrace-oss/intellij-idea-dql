package pl.thedeem.intellij.common.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DQLVerifyPayload {
    public String query;

    public DQLVerifyPayload(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
