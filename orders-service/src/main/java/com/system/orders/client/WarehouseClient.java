package com.system.orders.client;

import com.system.orders.dto.StockReservationResponse;

import com.system.orders.dto.WarehousePartRequest;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WarehouseClient {

    private final RestTemplate restTemplate;

    @Value("${services.warehouse.url:http://warehouse-service:8082}")
    private String warehouseBaseUrl;
    
    @Retry(name = "warehouseRetry")
    @CircuitBreaker(name = "warehouseCircuit")
    public Boolean reserveParts(Long orderId, Long branchId, Long masterId, List<WarehousePartRequest> parts) {

        String url = UriComponentsBuilder
                .fromHttpUrl(warehouseBaseUrl)
                .path("/api/stock/order/create")
                .queryParam("orderId", orderId)
                .queryParam("branchId", branchId)
                .queryParam("masterId", masterId)
                .toUriString();

        

        StockReservationResponse response = restTemplate.postForObject(url, parts, StockReservationResponse.class);

        log.info(
        		"Reserved parts: orderId={}, branchId={}, items={}",
        		orderId,
        		branchId,
        		parts.size()
        );
            
        if (response == null) {
        	throw new RuntimeException("Empty response from warehouse");
        }

        return response.getWaitingParts();
    }

    @Retry(name = "warehouseRetry")
    @CircuitBreaker(name = "warehouseCircuit")
    public void cancelReserve(Long orderId, Long branchId, Long masterId, List<WarehousePartRequest> parts) {

        String url = UriComponentsBuilder
                .fromHttpUrl(warehouseBaseUrl)
                .path("/api/stock/order/cancel")
                .queryParam("orderId", orderId)
                .queryParam("branchId", branchId)
                .queryParam("masterId", masterId)
                .toUriString();

        restTemplate.postForObject(url, parts, Void.class);

        log.info(
        		"Cancel reserve: orderId={}, branchId={}, items={}",
        		orderId,
        		branchId,
        		parts.size());
    }
    
}