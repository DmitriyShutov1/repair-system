package com.system.orders.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsersClient {

    private final RestTemplate restTemplate;

    @Value("${services.users.url:http://users-service:8081}")
    private String usersBaseUrl;

    public Boolean userExists(Long userId) {
        String url = UriComponentsBuilder
                .fromHttpUrl(usersBaseUrl)
                .path("/api/crud/users/{id}/exists")
                .buildAndExpand(userId)
                .toUriString();

        try {
            Boolean exists = restTemplate.getForObject(url, Boolean.class);
            log.info("User existence check: userId={}, exists={}", userId, exists);
            return exists;
        } catch (RestClientException e) {
            log.error("Failed to check user existence: userId={}", userId, e);
            return null;
        }
    }
}