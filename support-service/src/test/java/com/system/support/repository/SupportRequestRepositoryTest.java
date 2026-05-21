package com.system.support.repository;

import com.system.support.entity.SupportRequest;
import com.system.support.entity.SupportRequestStatus;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class SupportRequestRepositoryTest {

    @Autowired
    private SupportRequestRepository repository;

    @TestConfiguration
    static class TestConfig {

        @Bean
        public RestTemplateBuilder restTemplateBuilder() {
            return new RestTemplateBuilder();
        }
    }

    @Test
    void saveSupportRequest_success() {

        SupportRequest request = SupportRequest.builder()
                .supportId(1L)
                .branchId(1L)
                .clientId(20L)
                .masterId(10L)
                .orderId(100L)
                .description("Problem")
                .status(SupportRequestStatus.CREATED)
                .createdAt(Instant.now())
                .build();

        SupportRequest saved = repository.save(request);

        assertNotNull(saved.getId());

        SupportRequest found =
                repository.findById(saved.getId()).orElse(null);

        assertNotNull(found);

        assertEquals(100L, found.getOrderId());
    }

    @Test
    void findByOrderId_success() {

        SupportRequest request = SupportRequest.builder()
                .supportId(1L)
                .branchId(1L)
                .clientId(20L)
                .masterId(10L)
                .orderId(100L)
                .status(SupportRequestStatus.CREATED)
                .createdAt(Instant.now())
                .build();

        repository.save(request);

        List<SupportRequest> result =
                repository.findByOrderId(100L);

        assertFalse(result.isEmpty());
    }
}