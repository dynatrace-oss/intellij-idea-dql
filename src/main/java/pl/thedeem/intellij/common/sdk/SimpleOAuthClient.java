package pl.thedeem.intellij.common.sdk;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.util.Urls;
import com.intellij.xml.util.XmlStringUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.sdk.errors.DTAuthException;
import pl.thedeem.intellij.common.sdk.errors.SSOAuthException;
import pl.thedeem.intellij.common.sdk.errors.SSOReAuthRequiredException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SimpleOAuthClient {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String CALLBACK_PATH = "/auth/login";
    private static final String REDIRECT_URL_FORMAT = "http://localhost:%s" + CALLBACK_PATH;

    private final HttpClient httpClient;

    public SimpleOAuthClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public CompletableFuture<Map<String, Object>> startAuthFlow(
            String authUrl, String tokenUrl, String clientId, String resource, List<String> scopes, Consumer<String> openUrl) {

        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

        try {
            String codeVerifier = generateRandomString();
            String codeChallenge = generateCodeChallenge(codeVerifier);
            String state = generateRandomString();

            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            int callbackPort = server.getAddress().getPort();
            server.createContext(CALLBACK_PATH, exchange -> {
                try {
                    Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                    if (!Objects.equals(params.get("state"), state)) {
                        throw new DTAuthException("Security error: State mismatch.");
                    }

                    String code = params.get("code");
                    if (code == null) {
                        throw new DTAuthException("Authorization code not found in redirect.");
                    }

                    Map<String, Object> tokens = exchangeCodeForTokens(tokenUrl, clientId, code, codeVerifier, callbackPort);
                    sendHtmlResponse(exchange, 200, loadCallbackPage("/oauth/success.html", null));
                    future.complete(tokens);
                } catch (Exception e) {
                    sendHtmlResponse(exchange, 400, loadCallbackPage("/oauth/error.html", e.getMessage()));
                    future.completeExceptionally(e);
                } finally {
                    server.stop(0);
                }
            });

            server.start();

            future.whenComplete((r, ex) -> server.stop(0));
            CompletableFuture.delayedExecutor(5, TimeUnit.MINUTES).execute(() -> {
                if (!future.isDone()) {
                    future.completeExceptionally(new DTAuthException("Authorization timed out after 5 minutes."));
                }
            });

            Map<String, String> params = new LinkedHashMap<>();
            params.put("response_type", "code");
            params.put("client_id", clientId);
            params.put("redirect_uri", REDIRECT_URL_FORMAT.formatted(callbackPort));
            params.put("scope", String.join(" ", scopes));
            params.put("state", state);
            params.put("code_challenge", codeChallenge);
            params.put("code_challenge_method", "S256");
            params.put("resource", resource);

            openUrl.accept(Urls.newFromEncoded(authUrl).addParameters(params).toExternalForm());
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    public Map<String, Object> refreshToken(String tokenUrl, String clientId, String refreshToken) throws SSOAuthException, InterruptedException {
        return executeTokenRequest(tokenUrl, Map.of(
                "grant_type", "refresh_token",
                "refresh_token", refreshToken,
                "client_id", clientId
        ));
    }

    private Map<String, Object> exchangeCodeForTokens(String tokenUrl, String clientId, String code, String verifier, int port) throws SSOAuthException, InterruptedException {
        return executeTokenRequest(tokenUrl, Map.of(
                "grant_type", "authorization_code",
                "code", code,
                "client_id", clientId,
                "redirect_uri", REDIRECT_URL_FORMAT.formatted(port),
                "code_verifier", verifier
        ));
    }

    private Map<String, Object> executeTokenRequest(String url, Map<String, String> bodyParams) throws SSOAuthException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(buildFormBody(bodyParams)))
                .build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new SSOAuthException("Error when executing SSO request", e);
        }
        if (response.statusCode() == 400) {
            throw new SSOReAuthRequiredException("Invalid refresh token or authorization code. Re-authentication required: " + response.body());
        }
        if (response.statusCode() != 200) {
            throw new SSOAuthException("OAuth Server Error (" + response.statusCode() + "): " + response.body());
        }

        Map<String, Object> result;
        try {
            result = mapper.readValue(response.body(), new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new SSOAuthException("Failed to parse token response: " + e.getMessage(), e);
        }
        long now = Instant.now().toEpochMilli();
        long expiresInSeconds = Long.parseLong(result.getOrDefault("expires_in", "3600").toString());

        result.put("valid_from", now);
        result.put("valid_to", now + (expiresInSeconds * 1000L));

        return result;
    }

    private String generateRandomString() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateCodeChallenge(String verifier) throws NoSuchAlgorithmException {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    private String buildFormBody(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> URLEncodedUtils.format(List.of(new BasicNameValuePair(e.getKey(), e.getValue())), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    private void sendHtmlResponse(HttpExchange exchange, int code, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String loadCallbackPage(String path, @Nullable String error) {
        String defaultMessage = "Process completed. You can close this window.";
        if (error != null) {
            defaultMessage = "Error: " + XmlStringUtil.escapeString(error) + ". You can close this window.";
        }
        try (var is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                return defaultMessage;
            }
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return error != null ? content.replace("{{ERROR}}", XmlStringUtil.escapeString(error)) : content;
        } catch (IOException ignored) {
            return defaultMessage;
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        if (query == null || query.isEmpty()) return Map.of();
        return URLEncodedUtils.parse(query, StandardCharsets.UTF_8).stream()
                .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue, (a, b) -> a));
    }
}