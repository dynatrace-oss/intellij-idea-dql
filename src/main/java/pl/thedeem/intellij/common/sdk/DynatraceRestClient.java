package pl.thedeem.intellij.common.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.diagnostic.Logger;
import pl.thedeem.intellij.common.sdk.errors.*;
import pl.thedeem.intellij.common.sdk.model.*;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class DynatraceRestClient {
    private static final Logger logger = Logger.getInstance(DynatraceRestClient.class);
    private final static ObjectMapper mapper = new ObjectMapper();
    private final String tenantUrl;

    public DynatraceRestClient(String tenantUrl) {
        this.tenantUrl = tenantUrl;
    }

    public DQLVerifyResponse verifyDQL(DQLVerifyPayload payload, String authToken) throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(tenantUrl + "/platform/storage/query/v1/query:verify").normalize())
                    .method("POST", HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload)))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return handleResponse(response, new TypeReference<>() {
            });
        }
    }

    public DQLExecuteResponse executeDQL(DQLExecutePayload payload, String authToken) throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(tenantUrl + "/platform/storage/query/v1/query:execute?enrich=metric-metadata").normalize())
                    .method("POST", HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload)))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return handleResponse(response, new TypeReference<>() {
            });
        }
    }

    public DQLPollResponse cancelDQL(String requestToken, String authToken) throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(tenantUrl + "/platform/storage/query/v1/query:cancel?request-token=" + URLEncoder.encode(requestToken, StandardCharsets.UTF_8) + "&enrich=metric-metadata").normalize())
                    .method("POST", HttpRequest.BodyPublishers.ofString(""))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return handleResponse(response, new TypeReference<>() {
            });
        }
    }

    public DQLPollResponse pollDQLState(String requestToken, String authToken) throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(tenantUrl + "/platform/storage/query/v1/query:poll?enrich=metric-metadata&request-token=" + URLEncoder.encode(requestToken, StandardCharsets.UTF_8)).normalize())
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return handleResponse(response, new TypeReference<>() {
            });
        }
    }

    public DQLAutocompleteResult autocomplete(DQLAutocompletePayload payload, String authToken) throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(tenantUrl + "/platform/storage/query/v1/query:autocomplete").normalize())
                    .method("POST", HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload)))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return handleResponse(response, new TypeReference<>() {
            });
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, TypeReference<T> typeRef) throws DQLApiException {
        int status = response.statusCode();
        String body = response.body();
        try {
            if (status < 300) {
                return mapper.readValue(body, typeRef);
            } else if (status == 301 || status == 302) {
                String location = response.headers().firstValue("location").orElse(null);
                logger.warn(String.format("Could not get the correct response; the API call was redirected to %s. Response: %s", location, body));
                throw new DQLResponseRedirectedException("The request was redirected", location);
            } else if (status == 401 || status == 403) {
                logger.warn(String.format("Could not authorize the user. Reason: %s", body));
                throw new DQLNotAuthorizedException("Unauthorized", mapper.readValue(body, new TypeReference<>() {
                }));
            } else {
                logger.warn(String.format("Could not execute the query. Reason: %s", body));

                throw new DQLErrorResponseException("Error response", mapper.readValue(body, new TypeReference<>() {
                }));
            }
        } catch (JsonProcessingException jsonError) {
            logger.warn(String.format("The response returned by Dynatrace was not a JSON. String response: %s", body), jsonError);
            throw new DQLResponseParsingException("Response parsing error", body);
        }
    }
}
