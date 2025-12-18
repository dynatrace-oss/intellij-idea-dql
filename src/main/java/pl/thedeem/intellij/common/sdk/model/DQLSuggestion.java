package pl.thedeem.intellij.common.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DQLSuggestion {
    public Long alreadyTypedCharacters;
    public String suggestion;
    public List<DQLSuggestionPart> parts;

    public Long getAlreadyTypedCharacters() {
        return alreadyTypedCharacters;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public List<DQLSuggestionPart> getParts() {
        return parts != null ? parts : List.of();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DQLSuggestionPart {
        public String suggestion;
        public String type;
        public String info;

        public String getSuggestion() {
            return suggestion;
        }

        public String getType() {
            return type;
        }

        public String getInfo() {
            return info;
        }
    }
}
