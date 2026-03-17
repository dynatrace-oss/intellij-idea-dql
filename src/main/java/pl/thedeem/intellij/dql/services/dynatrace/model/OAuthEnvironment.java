package pl.thedeem.intellij.dql.services.dynatrace.model;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record OAuthEnvironment(
        List<String> domains,
        String authUrl,
        String tokenUrl,
        String clientId
) {
    public static @NotNull OAuthEnvironment empty() {
        return new OAuthEnvironment(List.of(), "", "", "");
    }
}
