package com.system.warehouse.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.system.warehouse.dto.OrderBranchPair;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrdersClient {

    private final RestTemplate restTemplate;

    private static final String ORDERS_SERVICE_URL = "http://orders-service:8083";

    @Retry(name = "ordersRetry")
    @CircuitBreaker(name = "ordersCircuit", fallbackMethod = "notifyFallback")
    public void notifyOrdersInProgress(Set<Long> orderIds) {

        if (orderIds == null || orderIds.isEmpty()) {
            return;
        }

        String url = ORDERS_SERVICE_URL + "/api/orders/set-in-progress";

        restTemplate.postForObject(url, orderIds, Void.class);

        log.info("Orders moved to IN_PROGRESS: {}", orderIds);
    }

    private void notifyFallback(Set<Long> orderIds, Throwable ex) {
        log.error("Notify orders fallback: {}", orderIds, ex);

        throw new RuntimeException("Orders service unavailable", ex);
    }



    @Retry(name = "ordersRetry")
    @CircuitBreaker(name = "ordersCircuit", fallbackMethod = "cancelFallback")
    public void cancelOrders(List<OrderBranchPair> pairs) {

        if (pairs == null || pairs.isEmpty()) {
            return;
        }

        String url = ORDERS_SERVICE_URL + "/api/orders/cancel-orders";

        restTemplate.postForObject(url, pairs, Void.class);

        log.info("Orders cancel requested: {}", pairs);
    }

    private void cancelFallback(List<OrderBranchPair> pairs, Throwable ex) {
        log.error("Cancel orders fallback: {}", pairs, ex);

        throw new RuntimeException("Orders service unavailable", ex);
    }
}