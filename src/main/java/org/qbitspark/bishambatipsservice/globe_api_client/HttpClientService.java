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
import java.util.Map;

/**
 * Unified HTTP Client Service for all HTTP operations
 * Supports: GET, POST, PUT, DELETE, PATCH
 * Authentication: None, Basic Auth, Bearer Token
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HttpClientService {

    private final WebClient webClient;

    @Value("${app.external.timeout:60000}")
    private int timeout;



    // ================================
    // 1. BASIC HTTP METHODS
    // ================================

    public <T> Mono<T> get(String url, Class<T> responseType) {
        return get(url, Map.of(), responseType);
    }

    public <T> Mono<T> get(String url, Map<String, String> headers, Class<T> responseType) {
        return executeGet(url, headers, responseType);
    }

    public <T, R> Mono<R> post(String url, T body, Class<R> responseType) {
        return executePost(url, body, Map.of("Content-Type", "application/json"), responseType);
    }

    public <T, R> Mono<R> post(String url, T body, Map<String, String> headers, Class<R> responseType) {
        return executePost(url, body, headers, responseType);
    }

    public <T, R> Mono<R> put(String url, T body, Class<R> responseType) {
        return executePut(url, body, Map.of("Content-Type", "application/json"), responseType);
    }

    public <T, R> Mono<R> put(String url, T body, Map<String, String> headers, Class<R> responseType) {
        return executePut(url, body, headers, responseType);
    }

    public <T, R> Mono<R> patch(String url, T body, Class<R> responseType) {
        return executePatch(url, body, Map.of("Content-Type", "application/json"), responseType);
    }

    public <T, R> Mono<R> patch(String url, T body, Map<String, String> headers, Class<R> responseType) {
        return executePatch(url, body, headers, responseType);
    }

    public <T> Mono<T> delete(String url, Class<T> responseType) {
        return executeDelete(url, Map.of(), responseType);
    }

    public <T> Mono<T> delete(String url, Map<String, String> headers, Class<T> responseType) {
        return executeDelete(url, headers, responseType);
    }

    // ================================
    // 2. BEARER TOKEN AUTHENTICATION
    // ================================

    public <T> Mono<T> getWithBearer(String url, String token, Class<T> responseType) {
        return get(url, bearerHeaders(token), responseType);
    }

    public <T, R> Mono<R> postWithBearer(String url, T body, String token, Class<R> responseType) {
        Map<String, String> headers = Map.of(
                "Authorization", "Bearer " + token,
                "Content-Type", "application/json"
        );
        return post(url, body, headers, responseType);
    }

    public <T, R> Mono<R> putWithBearer(String url, T body, String token, Class<R> responseType) {
        Map<String, String> headers = Map.of(
                "Authorization", "Bearer " + token,
                "Content-Type", "application/json"
        );
        return put(url, body, headers, responseType);
    }

    public <T, R> Mono<R> patchWithBearer(String url, T body, String token, Class<R> responseType) {
        Map<String, String> headers = Map.of(
                "Authorization", "Bearer " + token,
                "Content-Type", "application/json"
        );
        return patch(url, body, headers, responseType);
    }

    public <T> Mono<T> deleteWithBearer(String url, String token, Class<T> responseType) {
        return delete(url, bearerHeaders(token), responseType);
    }

    // ================================
    // 3. BASIC AUTHENTICATION
    // ================================

    public <T> Mono<T> getWithBasicAuth(String url, String username, String password, Class<T> responseType) {
        return get(url, basicAuthHeaders(username, password), responseType);
    }

    public <T, R> Mono<R> postWithBasicAuth(String url, T body, String username, String password, Class<R> responseType) {
        Map<String, String> headers = mergeHeaders(
                basicAuthHeaders(username, password),
                Map.of("Content-Type", "application/json")
        );
        return post(url, body, headers, responseType);
    }

    public <T, R> Mono<R> putWithBasicAuth(String url, T body, String username, String password, Class<R> responseType) {
        Map<String, String> headers = mergeHeaders(
                basicAuthHeaders(username, password),
                Map.of("Content-Type", "application/json")
        );
        return put(url, body, headers, responseType);
    }

    public <T, R> Mono<R> patchWithBasicAuth(String url, T body, String username, String password, Class<R> responseType) {
        Map<String, String> headers = mergeHeaders(
                basicAuthHeaders(username, password),
                Map.of("Content-Type", "application/json")
        );
        return patch(url, body, headers, responseType);
    }

    public <T> Mono<T> deleteWithBasicAuth(String url, String username, String password, Class<T> responseType) {
        return delete(url, basicAuthHeaders(username, password), responseType);
    }

    // ================================
    // 4. SYNCHRONOUS CONVENIENCE METHODS
    // ================================

    // Basic HTTP - Sync
    public <T> T getSync(String url, Class<T> responseType) {
        return get(url, responseType).block();
    }

    public <T, R> R postSync(String url, T body, Class<R> responseType) {
        return post(url, body, responseType).block();
    }

    public <T, R> R putSync(String url, T body, Class<R> responseType) {
        return put(url, body, responseType).block();
    }

    public <T, R> R patchSync(String url, T body, Class<R> responseType) {
        return patch(url, body, responseType).block();
    }

    public <T> T deleteSync(String url, Class<T> responseType) {
        return delete(url, responseType).block();
    }

    // Bearer Token - Sync
    public <T> T getWithBearerSync(String url, String token, Class<T> responseType) {
        return getWithBearer(url, token, responseType).block();
    }

    public <T, R> R postWithBearerSync(String url, T body, String token, Class<R> responseType) {
        return postWithBearer(url, body, token, responseType).block();
    }

    public <T, R> R putWithBearerSync(String url, T body, String token, Class<R> responseType) {
        return putWithBearer(url, body, token, responseType).block();
    }

    public <T, R> R patchWithBearerSync(String url, T body, String token, Class<R> responseType) {
        return patchWithBearer(url, body, token, responseType).block();
    }

    public <T> T deleteWithBearerSync(String url, String token, Class<T> responseType) {
        return deleteWithBearer(url, token, responseType).block();
    }

    // Basic Auth - Sync
    public <T> T getWithBasicAuthSync(String url, String username, String password, Class<T> responseType) {
        return getWithBasicAuth(url, username, password, responseType).block();
    }

    public <T, R> R postWithBasicAuthSync(String url, T body, String username, String password, Class<R> responseType) {
        return postWithBasicAuth(url, body, username, password, responseType).block();
    }

    public <T, R> R putWithBasicAuthSync(String url, T body, String username, String password, Class<R> responseType) {
        return putWithBasicAuth(url, body, username, password, responseType).block();
    }

    public <T, R> R patchWithBasicAuthSync(String url, T body, String username, String password, Class<R> responseType) {
        return patchWithBasicAuth(url, body, username, password, responseType).block();
    }

    public <T> T deleteWithBasicAuthSync(String url, String username, String password, Class<T> responseType) {
        return deleteWithBasicAuth(url, username, password, responseType).block();
    }

    // ================================
    // 5. UTILITY METHODS
    // ================================

    public <T> Mono<T> withRetry(Mono<T> operation, int maxRetries) {
        return operation.retry(maxRetries);
    }

    public <T> Mono<T> withCustomTimeout(Mono<T> operation, Duration customTimeout) {
        return operation.timeout(customTimeout);
    }

    // ================================
    // CORE EXECUTION ENGINE
    // ================================

    private <R> Mono<R> executeGet(String url, Map<String, String> headers, Class<R> responseType) {
        log.info("GET: {}", url);

        return webClient.get()
                .uri(url)
                .headers(h -> addHeaders(h, headers))
                .retrieve()
                .bodyToMono(responseType)
                .timeout(Duration.ofMillis(timeout))
                .doOnError(error -> log.error("GET failed: {}", url, error));
    }

    private <T, R> Mono<R> executePost(String url, T body, Map<String, String> headers, Class<R> responseType) {
        log.info("POST: {}", url);

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
                .doOnError(error -> log.error("POST failed: {}", url, error));
    }

    private <T, R> Mono<R> executePut(String url, T body, Map<String, String> headers, Class<R> responseType) {
        log.info("PUT: {}", url);

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
                .doOnError(error -> log.error("PUT failed: {}", url, error));
    }

    private <T, R> Mono<R> executePatch(String url, T body, Map<String, String> headers, Class<R> responseType) {
        log.info("PATCH: {}", url);

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
                .doOnError(error -> log.error("PATCH failed: {}", url, error));
    }

    private <R> Mono<R> executeDelete(String url, Map<String, String> headers, Class<R> responseType) {
        log.info("DELETE: {}", url);

        return webClient.delete()
                .uri(url)
                .headers(h -> addHeaders(h, headers))
                .retrieve()
                .bodyToMono(responseType)
                .timeout(Duration.ofMillis(timeout))
                .doOnError(error -> log.error("DELETE failed: {}", url, error));
    }

    // ================================
    // HELPER METHODS
    // ================================

    private void addHeaders(HttpHeaders httpHeaders, Map<String, String> headers) {
        if (headers != null) {
            headers.forEach(httpHeaders::add);
        }
    }

    private Map<String, String> bearerHeaders(String token) {
        return Map.of("Authorization", "Bearer " + token);
    }

    private Map<String, String> basicAuthHeaders(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);
        return Map.of("Authorization", authHeader);
    }

    private Map<String, String> mergeHeaders(Map<String, String> headers1, Map<String, String> headers2) {
        return Map.of(
                headers1.entrySet().iterator().next().getKey(),
                headers1.entrySet().iterator().next().getValue(),
                headers2.entrySet().iterator().next().getKey(),
                headers2.entrySet().iterator().next().getValue()
        );
    }
}

// ================================
// USAGE EXAMPLES
// ================================

/*
@Service
public class EmailService {

    private final HttpClientService httpClient;

    @Value("${email.api.username}")
    private String emailUsername;

    @Value("${email.api.password}")
    private String emailPassword;

    public EmailService(HttpClientService httpClient) {
        this.httpClient = httpClient;
    }

    // Email API call with basic auth
    public EmailResponse sendEmail(EmailRequest request) {
        return httpClient.postWithBasicAuthSync(
            "https://email-api.com/send",
            request,
            emailUsername,
            emailPassword,
            EmailResponse.class
        );
    }

    // Async email sending
    public Mono<EmailResponse> sendEmailAsync(EmailRequest request) {
        return httpClient.postWithBasicAuth(
            "https://email-api.com/send",
            request,
            emailUsername,
            emailPassword,
            EmailResponse.class
        );
    }
}

@Service
public class SmsService {

    private final HttpClientService httpClient;

    @Value("${sms.api.key}")
    private String smsApiKey;

    @Value("${sms.api.secret}")
    private String smsApiSecret;

    public SmsService(HttpClientService httpClient) {
        this.httpClient = httpClient;
    }

    // SMS with configured credentials
    public SmsResponse sendSms(SmsRequest request) {
        return httpClient.postWithBasicAuthSync(
            "https://sms-api.com/send",
            request,
            smsApiKey,
            smsApiSecret,
            SmsResponse.class
        );
    }

    // SMS with dynamic credentials (for multi-tenant scenarios)
    public SmsResponse sendSmsWithCredentials(SmsRequest request, String apiKey, String secret) {
        return httpClient.postWithBasicAuthSync(
            "https://sms-api.com/send",
            request,
            apiKey,
            secret,
            SmsResponse.class
        );
    }
}

@Service
public class PaymentService {

    private final HttpClientService httpClient;

    public PaymentService(HttpClientService httpClient) {
        this.httpClient = httpClient;
    }

    // Payment API with Bearer token
    public PaymentResponse processPayment(PaymentRequest request, String jwtToken) {
        return httpClient.postWithBearerSync(
            "https://payment-api.com/process",
            request,
            jwtToken,
            PaymentResponse.class
        );
    }

    // Get payment status
    public PaymentStatus getPaymentStatus(String paymentId, String jwtToken) {
        return httpClient.getWithBearerSync(
            "https://payment-api.com/status/" + paymentId,
            jwtToken,
            PaymentStatus.class
        );
    }

    // Update payment with PUT
    public PaymentResponse updatePayment(String paymentId, PaymentUpdateRequest request, String jwtToken) {
        return httpClient.putWithBearerSync(
            "https://payment-api.com/payments/" + paymentId,
            request,
            jwtToken,
            PaymentResponse.class
        );
    }
}

@Service
public class ExternalDataService {

    private final HttpClientService httpClient;

    @Value("${external.api.token}")
    private String apiToken;

    public ExternalDataService(HttpClientService httpClient) {
        this.httpClient = httpClient;
    }

    // Reactive external API call
    public Mono<ExternalDataDto> fetchDataAsync(String endpoint) {
        return httpClient.getWithBearer(
            "https://external-api.com/v1/" + endpoint,
            apiToken,
            ExternalDataDto.class
        );
    }

    // Synchronous call with retry
    public ExternalDataDto fetchDataWithRetry(String endpoint) {
        Mono<ExternalDataDto> operation = httpClient.getWithBearer(
            "https://external-api.com/v1/" + endpoint,
            apiToken,
            ExternalDataDto.class
        );
        return httpClient.withRetry(operation, 3).block();
    }

    // POST with custom headers
    public CreateResponse createData(CreateRequest request) {
        Map<String, String> headers = Map.of(
            "Authorization", "Bearer " + apiToken,
            "Content-Type", "application/json",
            "X-API-Version", "v1",
            "X-Client-ID", "my-app"
        );

        return httpClient.postSync(
            "https://external-api.com/v1/create",
            request,
            headers,
            CreateResponse.class
        );
    }
}

@Service
public class GenericApiService {

    private final HttpClientService httpClient;

    public GenericApiService(HttpClientService httpClient) {
        this.httpClient = httpClient;
    }

    // Plain GET without authentication
    public PublicDataDto getPublicData(String endpoint) {
        return httpClient.getSync(
            "https://public-api.com/" + endpoint,
            PublicDataDto.class
        );
    }

    // DELETE with custom timeout
    public void deleteResource(String resourceId, String bearerToken) {
        Mono<Void> operation = httpClient.deleteWithBearer(
            "https://api.example.com/resources/" + resourceId,
            bearerToken,
            Void.class
        );

        httpClient.withCustomTimeout(operation, Duration.ofMinutes(2)).block();
    }

    // PATCH operation
    public UpdateResponse partialUpdate(String resourceId, PatchRequest request, String bearerToken) {
        return httpClient.patchWithBearerSync(
            "https://api.example.com/resources/" + resourceId,
            request,
            bearerToken,
            UpdateResponse.class
        );
    }
}
*/
