package io.testomat.junit5;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.testomat.junit5.exception.TestomatApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
public class TestomatApiClient {
    private static final String BASE_URL = "https://app.testomat.io/api/reporter";
    private final String apiKey;
    private final Gson gson = new Gson();

    public TestomatApiClient() {
        this.apiKey = System.getenv("TESTOMATIO");
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            log.error("TESTOMATIO environment variable is not set. Testomat.io reporting will be disabled.");
        }
    }

    private String buildUrl(String path) {
        return BASE_URL + path + "?api_key=" + apiKey;
    }

    private Optional<String> executeRequest(HttpUriRequest request) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("API key not available. Skipping API call: {}", request.getURI());
            return Optional.empty();
        }

        logRequestDetails(request);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            log.info("Executing API request: {} {}",
                    request.getMethod(), request.getURI());

            try (CloseableHttpResponse response = httpClient.execute(request)) {

                int statusCode = response.getStatusLine().getStatusCode();
                String body = response.getEntity() != null
                        ? EntityUtils.toString(response.getEntity())
                        : "";

                if (statusCode >= 200 && statusCode < 300) {
                    log.info("API call successful ({}): {}", statusCode, body);
                    return Optional.ofNullable(body);
                }

                if (statusCode >= 400 && statusCode < 500) {
                    log.error("Client error {}: {}", statusCode, body);
                    throw new TestomatApiException(statusCode, body);
                }

                if (statusCode >= 500) {
                    log.error("Server error {}: {}", statusCode, body);
                    throw new TestomatApiException(statusCode, body);
                }

                return Optional.empty();
            }

        } catch (IOException e) {
            log.error("Network error while calling Testomat API", e);
            throw new RuntimeException("Testomat API connection failed", e);
        }
    }

    public Optional<String> createTestRun(String title) {
        if (apiKey == null || apiKey.isEmpty()) return Optional.empty();

        HttpPost request = new HttpPost(buildUrl(""));
        JsonObject payload = new JsonObject();
        payload.addProperty("title", title);
        try {
            request.setEntity(new StringEntity(gson.toJson(payload), StandardCharsets.UTF_8));
            request.setHeader("Content-Type", "application/json");
        } catch (RuntimeException e) {
            log.warn("Error encoding test run payload: " + e.getMessage());
            return Optional.empty();
        }
        return executeRequest(request);
    }

    public Optional<String> reportTest(String runUid, JsonObject testReportPayload) {
        if (runUid == null || apiKey == null || apiKey.isEmpty()) return Optional.empty();

        HttpPost request = new HttpPost(buildUrl("/" + runUid + "/testrun"));
        try {
            request.setEntity(new StringEntity(gson.toJson(testReportPayload), StandardCharsets.UTF_8));
            request.setHeader("Content-Type", "application/json");
        } catch (RuntimeException e) {
            log.warn("Error encoding test report payload: " + e.getMessage());
            return Optional.empty();
        }
        return executeRequest(request);
    }

    public Optional<String> finishTestRun(String runUid, double duration) {
        if (runUid == null || apiKey == null || apiKey.isEmpty()) return Optional.empty();

        HttpPut request = new HttpPut(buildUrl("/" + runUid));
        JsonObject payload = new JsonObject();
        payload.addProperty("status_event", "finish");
        payload.addProperty("duration", duration);
        try {
            request.setEntity(new StringEntity(gson.toJson(payload), StandardCharsets.UTF_8));
            request.setHeader("Content-Type", "application/json");
        } catch (RuntimeException e) {
            log.warn("Error encoding finish test run payload: " + e.getMessage());
            return Optional.empty();
        }
        return executeRequest(request);
    }

    private void logRequestDetails(HttpUriRequest request) {
        log.info("Request URI: " + request.getURI());
        log.info("Request Method: " + request.getMethod());

        if (request instanceof HttpEntityEnclosingRequestBase) {
            HttpEntityEnclosingRequestBase entityRequest = (HttpEntityEnclosingRequestBase) request;
            if (entityRequest.getEntity() != null) {
                try {
                    String requestBody = EntityUtils.toString(entityRequest.getEntity());
                    log.info("Request Body: " + requestBody);
                    entityRequest.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));
                } catch (IOException e) {
                    log.warn("Could not log request body", e);
                }
            }
        }
    }
}