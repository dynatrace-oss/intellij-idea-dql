package pl.thedeem.intellij.common.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DQLAutocompleteResult {
    public Boolean optional;
    public Long suggestedTtlSeconds;
    public List<DQLSuggestion> suggestions;

    public Boolean getOptional() {
        return optional;
    }

    public Long getSuggestedTtlSeconds() {
        return suggestedTtlSeconds;
    }

    public List<DQLSuggestion> getSuggestions() {
        return suggestions != null ? suggestions : List.of();
    }
}
