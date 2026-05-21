package com.system.warehouse.repository;

import com.system.warehouse.entity.Part;
import com.system.warehouse.entity.PartCategory;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PartRepositoryTest {

    @Autowired
    private PartRepository partRepository;

    @TestConfiguration
    static class TestConfig {

        @Bean
        public RestTemplateBuilder restTemplateBuilder() {
            return new RestTemplateBuilder();
        }
    }

    @Test
    void savePart_success() {

        Part part = Part.builder()
                .name("SSD")
                .articleNumber("SSD-001")
                .category(PartCategory.SSD)
                .active(true)
                .build();

        Part saved = partRepository.save(part);

        assertNotNull(saved.getId());

        Part found = partRepository
                .findById(saved.getId())
                .orElse(null);

        assertNotNull(found);

        assertEquals("SSD", found.getName());
    }

    @Test
    void existsByArticleNumber_success() {

        Part part = Part.builder()
                .name("SSD")
                .articleNumber("SSD-001")
                .category(PartCategory.SSD)
                .active(true)
                .build();

        partRepository.save(part);

        boolean exists =
                partRepository.existsByArticleNumber("SSD-001");

        assertTrue(exists);
    }
}