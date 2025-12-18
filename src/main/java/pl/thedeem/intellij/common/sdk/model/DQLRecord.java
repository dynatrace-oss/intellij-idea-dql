package pl.thedeem.intellij.common.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DQLRecord extends HashMap<String, Object> {
}
