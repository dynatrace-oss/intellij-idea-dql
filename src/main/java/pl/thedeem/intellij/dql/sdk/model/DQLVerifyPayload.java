package pl.thedeem.intellij.dql.sdk.model;

public class DQLVerifyPayload {
    public String query;

    public DQLVerifyPayload(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
