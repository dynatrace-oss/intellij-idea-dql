package pl.thedeem.intellij.common.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DQLErrorResponse {
    public Error error;
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Error(
            Integer code,
            String message,
            Map<String, Object> details
    ) {
    }
}
