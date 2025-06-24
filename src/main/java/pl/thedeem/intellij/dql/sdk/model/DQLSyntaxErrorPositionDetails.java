package pl.thedeem.intellij.dql.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DQLSyntaxErrorPositionDetails {
    public DQLSyntaxErrorPosition start;
    public DQLSyntaxErrorPosition end;

    public static final class DQLSyntaxErrorPosition {
        public Integer column;
        public Integer line;
        public Integer index;
    }

    public DQLSyntaxErrorPosition getStart() {
        return start;
    }

    public DQLSyntaxErrorPosition getEnd() {
        return end;
    }

    public Integer getStartIndex() {
        return start.index;
    }

    public Integer getEndIndex() {
        return end.index + 1;
    }
}
