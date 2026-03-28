package com.system.orders.client;

import com.system.orders.dto.StockReservationResponse;

import com.system.orders.dto.WarehousePartRequest;

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

    public Boolean reserveParts(Long orderId, Long branchId, Long masterId, List<WarehousePartRequest> parts) {

        String url = UriComponentsBuilder
                .fromHttpUrl(warehouseBaseUrl)
                .path("/api/stock/order/create")
                .queryParam("orderId", orderId)
                .queryParam("branchId", branchId)
                .queryParam("masterId", masterId)
                .toUriString();

        try {

        	StockReservationResponse response = restTemplate.postForObject(url, parts, StockReservationResponse.class);

            log.info(
                    "Reserved parts: orderId={}, branchId={}, items={}",
                    orderId,
                    branchId,
                    parts.size()
            );
            
            if (response == null) {
                return null;
            }

            return response.getWaitingParts();

        } catch (org.springframework.web.client.RestClientException e) {

            log.error(
                    "Warehouse reserve failed: orderId={}, branchId={}",
                    orderId,
                    branchId,
                    e
            );

            return null;
        }
    }

    public boolean cancelReserve(Long orderId, Long branchId, Long masterId, List<WarehousePartRequest> parts) {

        String url = UriComponentsBuilder
                .fromHttpUrl(warehouseBaseUrl)
                .path("/api/stock/order/cancel")
                .queryParam("orderId", orderId)
                .queryParam("branchId", branchId)
                .queryParam("masterId", masterId)
                .toUriString();

        try {

            restTemplate.postForObject(url, parts, Void.class);

            log.info(
                    "Cancel reserve: orderId={}, branchId={}, items={}",
                    orderId,
                    branchId,
                    parts.size()
            );

            return true;

        } catch (org.springframework.web.client.RestClientException e) {

            log.error(
                    "Warehouse cancel reserve failed: orderId={}, branchId={}",
                    orderId,
                    branchId,
                    e
            );

            return false;
        }
    }
}