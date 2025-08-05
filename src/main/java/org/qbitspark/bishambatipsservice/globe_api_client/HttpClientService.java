package org.qbitspark.bishambatipsservice.globe_api_client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Modern HTTP Client Service - Flexible & Comprehensive
 * Supports all HTTP methods with sync/async, authentication, and flexible headers
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HttpClientService {

    private final WebClient webClient;

    @Value("${app.external.timeout:60000}")
    private int timeout;

    // ================================
    // 1. CORE HTTP METHODS (SYNC)
    // ================================

    // GET methods
    public <T> T get(String url, Class<T> responseType) {
        return get(url, Map.of(), responseType);
    }

    public <T> T get(String url, Map<String, String> headers, Class<T> responseType) {
        return executeGet(url, headers, responseType).block();
    }

    // POST methods
    public <T, R> R post(String url, T body, Class<R> responseType) {
        return post(url, body, buildJsonHeaders(), responseType);
    }

    public <T, R> R post(String url, T body, Map<String, String> headers, Class<R> responseType) {
        return executePost(url, body, headers, responseType).block();
    }

    // PUT methods
    public <T, R> R put(String url, T body, Class<R> responseType) {
        return put(url, body, buildJsonHeaders(), responseType);
    }

    public <T, R> R put(String url, T body, Map<String, String> headers, Class<R> responseType) {
        return executePut(url, body, headers, responseType).block();
    }

    // PATCH methods
    public <T, R> R patch(String url, T body, Class<R> responseType) {
        return patch(url, body, buildJsonHeaders(), responseType);
    }

    public <T, R> R patch(String url, T body, Map<String, String> headers, Class<R> responseType) {
        return executePatch(url, body, headers, responseType).block();
    }

    // DELETE methods
    public <T> T delete(String url, Class<T> responseType) {
        return delete(url, Map.of(), responseType);
    }

    public <T> T delete(String url, Map<String, String> headers, Class<T> responseType) {
        return executeDelete(url, headers, responseType).block();
    }

    // ================================
    // 2. ASYNC METHODS
    // ================================

    public <T> Mono<T> getAsync(String url, Class<T> responseType) {
        return getAsync(url, Map.of(), responseType);
    }

    public <T> Mono<T> getAsync(String url, Map<String, String> headers, Class<T> responseType) {
        return executeGet(url, headers, responseType);
    }

    public <T, R> Mono<R> postAsync(String url, T body, Class<R> responseType) {
        return postAsync(url, body, buildJsonHeaders(), responseType);
    }

    public <T, R> Mono<R> postAsync(String url, T body, Map<String, String> headers, Class<R> responseType) {
        return executePost(url, body, headers, responseType);
    }

    public <T, R> Mono<R> putAsync(String url, T body, Class<R> responseType) {
        return putAsync(url, body, buildJsonHeaders(), responseType);
    }

    public <T, R> Mono<R> putAsync(String url, T body, Map<String, String> headers, Class<R> responseType) {
        return executePut(url, body, headers, responseType);
    }

    public <T, R> Mono<R> patchAsync(String url, T body, Class<R> responseType) {
        return patchAsync(url, body, buildJsonHeaders(), responseType);
    }

    public <T, R> Mono<R> patchAsync(String url, T body, Map<String, String> headers, Class<R> responseType) {
        return executePatch(url, body, headers, responseType);
    }

    public <T> Mono<T> deleteAsync(String url, Class<T> responseType) {
        return deleteAsync(url, Map.of(), responseType);
    }

    public <T> Mono<T> deleteAsync(String url, Map<String, String> headers, Class<T> responseType) {
        return executeDelete(url, headers, responseType);
    }

    // ================================
    // 3. AUTHENTICATION METHODS
    // ================================

    // Bearer Token - Sync
    public <T> T getWithBearer(String url, String token, Class<T> responseType) {
        return get(url, buildBearerHeaders(token), responseType);
    }

    public <T, R> R postWithBearer(String url, T body, String token, Class<R> responseType) {
        return post(url, body, buildBearerHeaders(token), responseType);
    }

    public <T, R> R putWithBearer(String url, T body, String token, Class<R> responseType) {
        return put(url, body, buildBearerHeaders(token), responseType);
    }

    public <T, R> R patchWithBearer(String url, T body, String token, Class<R> responseType) {
        return patch(url, body, buildBearerHeaders(token), responseType);
    }

    public <T> T deleteWithBearer(String url, String token, Class<T> responseType) {
        return delete(url, buildBearerHeaders(token), responseType);
    }

    // Basic Auth - Sync
    public <T> T getWithBasicAuth(String url, String username, String password, Class<T> responseType) {
        return get(url, buildBasicAuthHeaders(username, password), responseType);
    }

    public <T, R> R postWithBasicAuth(String url, T body, String username, String password, Class<R> responseType) {
        return post(url, body, buildBasicAuthHeaders(username, password), responseType);
    }

    public <T, R> R putWithBasicAuth(String url, T body, String username, String password, Class<R> responseType) {
        return put(url, body, buildBasicAuthHeaders(username, password), responseType);
    }

    public <T, R> R patchWithBasicAuth(String url, T body, String username, String password, Class<R> responseType) {
        return patch(url, body, buildBasicAuthHeaders(username, password), responseType);
    }

    public <T> T deleteWithBasicAuth(String url, String username, String password, Class<T> responseType) {
        return delete(url, buildBasicAuthHeaders(username, password), responseType);
    }

    // Bearer Token - Async
    public <T> Mono<T> getWithBearerAsync(String url, String token, Class<T> responseType) {
        return getAsync(url, buildBearerHeaders(token), responseType);
    }

    public <T, R> Mono<R> postWithBearerAsync(String url, T body, String token, Class<R> responseType) {
        return postAsync(url, body, buildBearerHeaders(token), responseType);
    }

    // Basic Auth - Async
    public <T> Mono<T> getWithBasicAuthAsync(String url, String username, String password, Class<T> responseType) {
        return getAsync(url, buildBasicAuthHeaders(username, password), responseType);
    }

    public <T, R> Mono<R> postWithBasicAuthAsync(String url, T body, String username, String password, Class<R> responseType) {
        return postAsync(url, body, buildBasicAuthHeaders(username, password), responseType);
    }

    // ================================
    // 4. HEADER BUILDERS
    // ================================

    public Map<String, String> buildHeaders() {
        return new HashMap<>();
    }

    public Map<String, String> buildJsonHeaders() {
        return Map.of("Content-Type", "application/json");
    }

    public Map<String, String> buildJsonHeaders(Map<String, String> additionalHeaders) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        if (additionalHeaders != null) {
            headers.putAll(additionalHeaders);
        }
        return headers;
    }

    public Map<String, String> buildBearerHeaders(String token) {
        return buildJsonHeaders(Map.of("Authorization", "Bearer " + token));
    }

    public Map<String, String> buildBasicAuthHeaders(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);
        return buildJsonHeaders(Map.of("Authorization", authHeader));
    }

    public Map<String, String> buildCustomAuthHeaders(String authType, String credentials) {
        return buildJsonHeaders(Map.of("Authorization", authType + " " + credentials));
    }

    public Map<String, String> buildApiKeyHeaders(String keyName, String keyValue) {
        return buildJsonHeaders(Map.of(keyName, keyValue));
    }

    public Map<String, String> buildHeaders(Map<String, String> headers) {
        Map<String, String> result = new HashMap<>();
        if (headers != null) {
            result.putAll(headers);
        }
        return result;
    }

    // ================================
    // 5. UTILITY METHODS
    // ================================

    public <T> T withRetry(Supplier<T> operation, int maxRetries, String operationName) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries + 1; attempt++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                if (attempt <= maxRetries) {
                    log.warn("Attempt {} failed for {}: {}. Retrying...", attempt, operationName, e.getMessage());
                    try {
                        Thread.sleep(1000 * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new HttpClientException("Operation interrupted during retry", ie);
                    }
                }
            }
        }

        throw new HttpClientException(operationName + " failed after " + (maxRetries + 1) + " attempts", lastException);
    }

    public <T> Mono<T> withRetryAsync(Mono<T> operation, int maxRetries) {
        return operation.retry(maxRetries);
    }

    public <T> Mono<T> withCustomTimeout(Mono<T> operation, Duration customTimeout) {
        return operation.timeout(customTimeout);
    }

    public <T> T executeWithErrorHandling(Supplier<T> httpCall, String operationName) {
        try {
            return httpCall.get();
        } catch (Exception e) {
            log.error("{} failed: {}", operationName, e.getMessage(), e);
            throw new HttpClientException(operationName + " failed: " + e.getMessage(), e);
        }
    }

    // ================================
    // 6. FLUENT BUILDER API
    // ================================

    public HttpRequestBuilder request() {
        return new HttpRequestBuilder(this);
    }

    public static class HttpRequestBuilder {
        private final HttpClientService httpClient;
        private String url;
        private String method = "GET";
        private Object body;
        private Map<String, String> headers = new HashMap<>();
        private Class<?> responseType = String.class;
        private boolean async = false;
        private int retries = 0;
        private Duration customTimeout;

        public HttpRequestBuilder(HttpClientService httpClient) {
            this.httpClient = httpClient;
        }

        public HttpRequestBuilder url(String url) {
            this.url = url;
            return this;
        }

        public HttpRequestBuilder get() {
            this.method = "GET";
            return this;
        }

        public HttpRequestBuilder post(Object body) {
            this.method = "POST";
            this.body = body;
            return this;
        }

        public HttpRequestBuilder put(Object body) {
            this.method = "PUT";
            this.body = body;
            return this;
        }

        public HttpRequestBuilder patch(Object body) {
            this.method = "PATCH";
            this.body = body;
            return this;
        }

        public HttpRequestBuilder delete() {
            this.method = "DELETE";
            return this;
        }

        public HttpRequestBuilder header(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        public HttpRequestBuilder headers(Map<String, String> headers) {
            if (headers != null) {
                this.headers.putAll(headers);
            }
            return this;
        }

        public HttpRequestBuilder contentType(String contentType) {
            this.headers.put("Content-Type", contentType);
            return this;
        }

        public HttpRequestBuilder json() {
            return contentType("application/json");
        }

        public HttpRequestBuilder auth(String type, String credentials) {
            this.headers.put("Authorization", type + " " + credentials);
            return this;
        }

        public HttpRequestBuilder bearer(String token) {
            return auth("Bearer", token);
        }

        public HttpRequestBuilder basicAuth(String username, String password) {
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
            String authHeader = "Basic " + new String(encodedAuth);
            this.headers.put("Authorization", authHeader);
            return this;
        }

        public HttpRequestBuilder apiKey(String keyName, String keyValue) {
            this.headers.put(keyName, keyValue);
            return this;
        }

        public HttpRequestBuilder responseType(Class<?> responseType) {
            this.responseType = responseType;
            return this;
        }

        public HttpRequestBuilder async() {
            this.async = true;
            return this;
        }

        public HttpRequestBuilder retry(int maxRetries) {
            this.retries = maxRetries;
            return this;
        }

        public HttpRequestBuilder timeout(Duration timeout) {
            this.customTimeout = timeout;
            return this;
        }

        @SuppressWarnings("unchecked")
        public <T> T execute() {
            if (async) {
                return (T) executeAsync();
            }

            Supplier<T> operation = () -> {
                return switch (method.toUpperCase()) {
                    case "GET" -> (T) httpClient.get(url, headers, (Class<T>) responseType);
                    case "POST" -> (T) httpClient.post(url, body, headers, (Class<T>) responseType);
                    case "PUT" -> (T) httpClient.put(url, body, headers, (Class<T>) responseType);
                    case "PATCH" -> (T) httpClient.patch(url, body, headers, (Class<T>) responseType);
                    case "DELETE" -> (T) httpClient.delete(url, headers, (Class<T>) responseType);
                    default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
                };
            };

            if (retries > 0) {
                return httpClient.withRetry(operation, retries, method + " " + url);
            } else {
                return operation.get();
            }
        }

        @SuppressWarnings("unchecked")
        public <T> Mono<T> executeAsync() {
            Mono<T> mono = switch (method.toUpperCase()) {
                case "GET" -> (Mono<T>) httpClient.getAsync(url, headers, (Class<T>) responseType);
                case "POST" -> (Mono<T>) httpClient.postAsync(url, body, headers, (Class<T>) responseType);
                case "PUT" -> (Mono<T>) httpClient.putAsync(url, body, headers, (Class<T>) responseType);
                case "PATCH" -> (Mono<T>) httpClient.patchAsync(url, body, headers, (Class<T>) responseType);
                case "DELETE" -> (Mono<T>) httpClient.deleteAsync(url, headers, (Class<T>) responseType);
                default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
            };

            if (retries > 0) {
                mono = httpClient.withRetryAsync(mono, retries);
            }

            if (customTimeout != null) {
                mono = httpClient.withCustomTimeout(mono, customTimeout);
            }

            return mono;
        }
    }

    // ================================
    // 7. CORE EXECUTION ENGINE
    // ================================

    private <R> Mono<R> executeGet(String url, Map<String, String> headers, Class<R> responseType) {
        log.debug("GET: {} -> {}", url, responseType.getSimpleName());

        return webClient.get()
                .uri(url)
                .headers(h -> addHeaders(h, headers))
                .retrieve()
                .bodyToMono(responseType)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(response -> log.debug("GET success: {} -> {}", url, response.getClass().getSimpleName()))
                .doOnError(error -> log.error("GET failed: {} -> {}", url, error.getMessage()));
    }

    private <T, R> Mono<R> executePost(String url, T body, Map<String, String> headers, Class<R> responseType) {
        log.debug("POST: {} -> {}", url, responseType.getSimpleName());

        WebClient.RequestBodySpec spec = webClient.post().uri(url);
        spec = spec.headers(h -> addHeaders(h, headers));

        WebClient.RequestHeadersSpec<?> headerSpec;
        if (body != null) {
            headerSpec = spec.bodyValue(body);
        } else {
            headerSpec = spec;
        }

        return headerSpec.retrieve()
                .bodyToMono(responseType)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(response -> log.debug("POST success: {} -> {}", url, response.getClass().getSimpleName()))
                .doOnError(error -> log.error("POST failed: {} -> {}", url, error.getMessage()));
    }

    private <T, R> Mono<R> executePut(String url, T body, Map<String, String> headers, Class<R> responseType) {
        log.debug("PUT: {} -> {}", url, responseType.getSimpleName());

        WebClient.RequestBodySpec spec = webClient.put().uri(url);
        spec = spec.headers(h -> addHeaders(h, headers));

        WebClient.RequestHeadersSpec<?> headerSpec;
        if (body != null) {
            headerSpec = spec.bodyValue(body);
        } else {
            headerSpec = spec;
        }

        return headerSpec.retrieve()
                .bodyToMono(responseType)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(response -> log.debug("PUT success: {} -> {}", url, response.getClass().getSimpleName()))
                .doOnError(error -> log.error("PUT failed: {} -> {}", url, error.getMessage()));
    }

    private <T, R> Mono<R> executePatch(String url, T body, Map<String, String> headers, Class<R> responseType) {
        log.debug("PATCH: {} -> {}", url, responseType.getSimpleName());

        WebClient.RequestBodySpec spec = webClient.patch().uri(url);
        spec = spec.headers(h -> addHeaders(h, headers));

        WebClient.RequestHeadersSpec<?> headerSpec;
        if (body != null) {
            headerSpec = spec.bodyValue(body);
        } else {
            headerSpec = spec;
        }

        return headerSpec.retrieve()
                .bodyToMono(responseType)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(response -> log.debug("PATCH success: {} -> {}", url, response.getClass().getSimpleName()))
                .doOnError(error -> log.error("PATCH failed: {} -> {}", url, error.getMessage()));
    }

    private <R> Mono<R> executeDelete(String url, Map<String, String> headers, Class<R> responseType) {
        log.debug("DELETE: {} -> {}", url, responseType.getSimpleName());

        return webClient.delete()
                .uri(url)
                .headers(h -> addHeaders(h, headers))
                .retrieve()
                .bodyToMono(responseType)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(response -> log.debug("DELETE success: {} -> {}", url, response.getClass().getSimpleName()))
                .doOnError(error -> log.error("DELETE failed: {} -> {}", url, error.getMessage()));
    }

    // ================================
    // 8. HELPER METHODS
    // ================================

    private void addHeaders(HttpHeaders httpHeaders, Map<String, String> headers) {
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(httpHeaders::add);
        }
    }

    // ================================
    // 9. CUSTOM EXCEPTION
    // ================================

    public static class HttpClientException extends RuntimeException {
        public HttpClientException(String message) {
            super(message);
        }

        public HttpClientException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}