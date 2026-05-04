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
import java.util.concurrent.atomic.AtomicLong;

/**
 * Shared HTTP/2-capable requester with endpoint failover.
 */
public class Http2Requester {

    private static final Map<Duration, HttpClient> CLIENTS = new ConcurrentHashMap<>();

    private static final Map<URI, AtomicLong> UNHEALTHY_UNTIL_NANOS = new ConcurrentHashMap<>();

    private Http2Requester() {
    }

    public static String executeGET(Config config, String path) {
        return executeGET(config, path, null);
    }

    public static String executeGET(Config config, String path, Map<String, String> params) {
        return execute(config, "GET", path, params);
    }

    public static String executePOST(Config config, String path) {
        return execute(config, "POST", path, null);
    }

    private static String execute(Config config, String method, String path, Map<String, String> params) {
        List<URI> endpoints = config.getUidGeneratorServerUris();
        if (endpoints == null || endpoints.isEmpty()) {
            throw new ClientValidationException("At least one uid generator server URI is required");
        }

        int attempts = Math.max(1, config.getMaxRetries() + 1);
        int startIndex = config.nextEndpointIndex(endpoints.size());
        ClientHttpRequestException lastError = null;
        for (int attempt = 0; attempt < attempts; attempt++) {
            URI baseUri = selectEndpoint(endpoints, startIndex + attempt);
            URI uri = buildUri(baseUri, path, params);
            try {
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri)
                    .timeout(config.getReadTimeout())
                    .header("Accept", "application/json");
                if ("POST".equals(method)) {
                    requestBuilder.POST(HttpRequest.BodyPublishers.noBody());
                } else {
                    requestBuilder.GET();
                }
                if (hasText(config.getToken())) {
                    requestBuilder.header("Authorization", "Bearer " + config.getToken());
                }
                HttpResponse<String> response = client(config.getConnectTimeout())
                    .send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    return response.body();
                }
                markUnhealthy(baseUri, config.getFailurePenalty());
                lastError = new ClientHttpRequestException(
                    "HTTP Code:" + response.statusCode() + ", Message:" + response.body());
            } catch (IOException ex) {
                markUnhealthy(baseUri, config.getFailurePenalty());
                lastError = new ClientHttpRequestException("HTTP request failed: " + uri, ex);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new ClientHttpRequestException("HTTP request interrupted: " + uri, ex);
            }
            backoff(config, attempt, attempts);
        }
        throw lastError == null ? new ClientHttpRequestException("HTTP request failed") : lastError;
    }

    private static URI selectEndpoint(List<URI> endpoints, int startIndex) {
        long now = System.nanoTime();
        for (int offset = 0; offset < endpoints.size(); offset++) {
            URI endpoint = endpoints.get(Math.floorMod(startIndex + offset, endpoints.size()));
            AtomicLong unhealthyUntil = UNHEALTHY_UNTIL_NANOS.get(endpoint);
            if (unhealthyUntil == null || unhealthyUntil.get() <= now) {
                return endpoint;
            }
        }
        return endpoints.get(Math.floorMod(startIndex, endpoints.size()));
    }

    private static void markUnhealthy(URI endpoint, Duration penalty) {
        Duration safePenalty = penalty == null ? Duration.ofSeconds(1) : penalty;
        if (safePenalty.isZero() || safePenalty.isNegative()) {
            return;
        }
        long until = System.nanoTime() + safePenalty.toNanos();
        UNHEALTHY_UNTIL_NANOS.computeIfAbsent(endpoint, key -> new AtomicLong()).set(until);
    }

    private static void backoff(Config config, int attempt, int attempts) {
        Duration backoff = config.getRetryBackoff();
        if (attempt >= attempts - 1 || backoff == null || backoff.isZero() || backoff.isNegative()) {
            return;
        }
        try {
            Thread.sleep(backoff.toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ClientHttpRequestException("HTTP retry interrupted", ex);
        }
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
