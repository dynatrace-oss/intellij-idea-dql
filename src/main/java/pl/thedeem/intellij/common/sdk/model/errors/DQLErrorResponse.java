package pl.thedeem.intellij.common.sdk.model.errors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DQLErrorResponse<T> {
    public T error;
}
