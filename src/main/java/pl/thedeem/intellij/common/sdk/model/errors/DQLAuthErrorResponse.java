package pl.thedeem.intellij.common.sdk.model.errors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DQLAuthErrorResponse {
    public Integer code;
    public String message;
    public Map<String, Object> details;
}
