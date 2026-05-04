package com.github.mxsm.rain.uid.client;

import com.github.mxsm.rain.uid.client.exception.ClientHttpRequestException;
import com.github.mxsm.rain.uid.client.exception.ClientValidationException;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared HTTP/2-capable requester with endpoint failover.
 */
public class Http2Requester {

    private static final Map<Duration, HttpClient> CLIENTS = new ConcurrentHashMap<>();

    private Http2Requester() {
    }

    public static String executeGET(Config config, String path) {
        return executeGET(config, path, null);
    }

    public static String executeGET(Config config, String path, Map<String, String> params) {
        List<URI> endpoints = config.getUidGeneratorServerUris();
        if (endpoints == null || endpoints.isEmpty()) {
            throw new ClientValidationException("At least one uid generator server URI is required");
        }

        int attempts = Math.max(1, config.getMaxRetries() + 1);
        int startIndex = config.nextEndpointIndex(endpoints.size());
        ClientHttpRequestException lastError = null;
        for (int attempt = 0; attempt < attempts; attempt++) {
            URI baseUri = endpoints.get((startIndex + attempt) % endpoints.size());
            URI uri = buildUri(baseUri, path, params);
            try {
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri)
                    .timeout(config.getReadTimeout())
                    .GET()
                    .header("Accept", "application/json");
                if (hasText(config.getToken())) {
                    requestBuilder.header("Authorization", "Bearer " + config.getToken());
                }
                HttpResponse<String> response = client(config.getConnectTimeout())
                    .send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    return response.body();
                }
                lastError = new ClientHttpRequestException(
                    "HTTP Code:" + response.statusCode() + ", Message:" + response.body());
            } catch (IOException ex) {
                lastError = new ClientHttpRequestException("HTTP request failed: " + uri, ex);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new ClientHttpRequestException("HTTP request interrupted: " + uri, ex);
            }
        }
        throw lastError == null ? new ClientHttpRequestException("HTTP request failed") : lastError;
    }

    private static HttpClient client(Duration connectTimeout) {
        Duration timeout = connectTimeout == null ? Duration.ofSeconds(3) : connectTimeout;
        return CLIENTS.computeIfAbsent(timeout, key -> HttpClient.newBuilder()
            .connectTimeout(key)
            .version(HttpClient.Version.HTTP_2)
            .build());
    }

    private static URI buildUri(URI baseUri, String path, Map<String, String> params) {
        String base = baseUri.toString().replaceAll("/+$", "");
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        StringBuilder builder = new StringBuilder(base).append(normalizedPath);
        if (params != null && !params.isEmpty()) {
            builder.append('?');
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!first) {
                    builder.append('&');
                }
                builder.append(encode(entry.getKey())).append('=').append(encode(entry.getValue()));
                first = false;
            }
        }
        return URI.create(builder.toString());
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
