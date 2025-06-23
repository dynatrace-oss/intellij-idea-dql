package pl.thedeem.intellij.dql.sdk.model;

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
        return !"RUNNING".equals(state);
    }
}

