package pl.thedeem.intellij.dql.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DQLPollResponse {
    public String state;
    public Long progress;
    public DQLResult result;

    public String getState() {
        return state;
    }

    public Long getProgress() {
        return progress;
    }

    public DQLResult getResult() {
        return result;
    }

    public boolean isFinished() {
        return !List.of("RUNNING", "NOT_STARTED").contains(state);
    }
}

