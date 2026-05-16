package com.system.orders.client;

import com.system.orders.dto.OrderItemRequest;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupportClient {

    private final RestTemplate restTemplate;

    @Value("${services.support.url:http://support-service:8084}")
    private String supportBaseUrl;
    
    @Retry(name = "supportRetry")
    @CircuitBreaker(name = "supportCircuit", fallbackMethod = "fallbackVoid")
    public void cancelWarranty(Long supportRequestId, boolean refund) {

        String url = UriComponentsBuilder
                .fromHttpUrl(supportBaseUrl)
                .path("/api/support-requests/{id}/cancel-warranty")
                .queryParam("refund", refund)
                .buildAndExpand(supportRequestId)
                .toUriString();

        restTemplate.postForEntity(url, null, Void.class);

        log.info("Cancel warranty: supportRequestId={}, refund={}",
                supportRequestId, refund);
    }

    private void fallbackVoid(Long supportRequestId, boolean refund, Throwable ex) {
        log.error("Support fallback triggered: supportRequestId={}", supportRequestId, ex);
        throw new RuntimeException("Support service unavailable", ex);
    }
    
    
    @Retry(name = "supportRetry")
    @CircuitBreaker(name = "supportCircuit", fallbackMethod = "fallbackVoid")
    public void startWarranty(Long supportRequestId, Long masterId) {

        String url = UriComponentsBuilder
                .fromHttpUrl(supportBaseUrl)
                .path("/api/support-requests/{id}/start-warranty")
                .buildAndExpand(supportRequestId)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", String.valueOf(masterId));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);

        log.info("Start warranty: supportRequestId={}, masterId={}",
                supportRequestId, masterId);
    }
    
    @Retry(name = "supportRetry")
    @CircuitBreaker(name = "supportCircuit", fallbackMethod = "fallbackVoidComplete")
    public void completeWarranty(
            Long supportRequestId,
            Long masterId,
            List<OrderItemRequest> parts) {

        String url = UriComponentsBuilder
                .fromHttpUrl(supportBaseUrl)
                .path("/api/support-requests/{id}/complete-warranty")
                .buildAndExpand(supportRequestId)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", String.valueOf(masterId));

        HttpEntity<List<OrderItemRequest>> entity =
                new HttpEntity<>(parts, headers);

        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);

        log.info("Complete warranty: supportRequestId={}, masterId={}, parts={}",
                supportRequestId, masterId, parts.size());
    }
    
    private void fallbackVoid(Long supportRequestId, Long masterId, Throwable ex) {
        log.error("Support start warranty fallback: id={}", supportRequestId, ex);
        throw new RuntimeException("Support unavailable", ex);
    }

    private void fallbackVoidComplete(Long supportRequestId, Long masterId,
                                     List<OrderItemRequest> parts, Throwable ex) {
        log.error("Support complete fallback: id={}", supportRequestId, ex);
        throw new RuntimeException("Support unavailable", ex);
    }
}
