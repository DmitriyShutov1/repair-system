package com.system.users.client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Slf4j
@Component
@RequiredArgsConstructor
public class WarehouseOrdersClient {

    private final RestTemplate restTemplate;

    private static final String ORDERS_SERVICE_URL = "http://orders-service:8083";
    private static final String WAREHOUSE_SERVICE_URL = "http://warehouse-service:8082";

    public boolean hasMasterActiveOrders(Long masterId) {
       
            String url = ORDERS_SERVICE_URL
                    + "/api/orders/master/" + masterId + "/has-active-orders";

            Boolean result = restTemplate.getForObject(url, Boolean.class);
            return Boolean.TRUE.equals(result);

        
    }

    @Retry(name = "warehouseRetry")
    @CircuitBreaker(name = "warehouseCircuit", fallbackMethod = "deleteStockFallback")
    public void deleteAllStockByBranch(Long branchId) {

        String url = WAREHOUSE_SERVICE_URL + "/api/stock/branch/" + branchId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Role", "ADMIN");

        HttpEntity<?> entity = new HttpEntity<>(headers);

        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);

        log.info("Deleted warehouse stock for branch={}", branchId);
    }

    private void deleteStockFallback(Long branchId, Throwable ex) {
        log.error("Warehouse delete stock fallback branch={}", branchId, ex);
        throw new RuntimeException("Warehouse unavailable", ex);
    }
}