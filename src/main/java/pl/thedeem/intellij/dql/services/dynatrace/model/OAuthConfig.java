package pl.thedeem.intellij.dql.services.dynatrace.model;

import java.util.List;

public record OAuthConfig(
        List<String> defaultScopes,
        List<OAuthEnvironment> environments
) {
    public static OAuthConfig empty() {
        return new OAuthConfig(List.of(), List.of());
    }
}
