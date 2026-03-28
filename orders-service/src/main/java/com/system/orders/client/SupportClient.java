package com.system.orders.client;

import com.system.orders.dto.OrderItemRequest;

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
    
    public Boolean startWarranty(Long supportRequestId, Long masterId) {

        String url = UriComponentsBuilder
                .fromHttpUrl(supportBaseUrl)
                .path("/api/support-requests/{id}/start-warranty")
                .buildAndExpand(supportRequestId)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", String.valueOf(masterId));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {

            restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Void.class
            );

            log.info("Start warranty sent: supportRequestId={}, masterId={}",
                    supportRequestId, masterId);

            return true;

        } catch (RestClientException e) {

            log.error("Failed start warranty: supportRequestId={}",
                    supportRequestId, e);

            return null;
        }
    }
    
    public Boolean cancelWarranty(Long supportRequestId, boolean refund) {

        String url = UriComponentsBuilder
                .fromHttpUrl(supportBaseUrl)
                .path("/api/support-requests/{id}/cancel-warranty")
                .queryParam("refund", refund)
                .buildAndExpand(supportRequestId)
                .toUriString();

        try {

            restTemplate.postForEntity(
                    url,
                    null,
                    Void.class
            );

            log.info("Cancel warranty: supportRequestId={}, refund={}",
                    supportRequestId, refund);

            return true;

        } catch (RestClientException e) {

            log.error("Failed cancel warranty: supportRequestId={}",
                    supportRequestId, e);

            return null;
        }
    }
    
    public Boolean completeWarranty(
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

        try {

            restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Void.class
            );

            log.info("Complete warranty: supportRequestId={}, masterId={}, parts={}",
                    supportRequestId, masterId, parts.size());

            return true;

        } catch (RestClientException e) {

            log.error("Failed complete warranty: supportRequestId={}",
                    supportRequestId, e);

            return null;
        }
    }
}