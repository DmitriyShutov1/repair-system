package com.system.users.repository;

import com.system.users.entity.UserAccount;

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
class UserAccountRepositoryTest {

    @Autowired
    private UserAccountRepository userRepository;

    @TestConfiguration
    static class TestConfig {

        @Bean
        public RestTemplateBuilder restTemplateBuilder() {
            return new RestTemplateBuilder();
        }
    }

    @Test
    void saveUser_success() {

        UserAccount user = UserAccount.builder()
                .email("test@test.com")
                .phone("123456")
                .passwordHash("hash")
                .role(UserAccount.Role.CLIENT)
                .status(UserAccount.Status.ACTIVE)
                .build();

        UserAccount saved = userRepository.save(user);

        assertNotNull(saved.getId());

        UserAccount found = userRepository.findById(saved.getId())
                .orElse(null);

        assertNotNull(found);

        assertEquals("test@test.com", found.getEmail());

        assertEquals(UserAccount.Role.CLIENT, found.getRole());
    }

    @Test
    void existsByPhone_success() {

        UserAccount user = UserAccount.builder()
                .email("phone@test.com")
                .phone("777777")
                .passwordHash("hash")
                .role(UserAccount.Role.CLIENT)
                .status(UserAccount.Status.ACTIVE)
                .build();

        userRepository.save(user);

        boolean exists = userRepository.existsByPhone("777777");

        assertTrue(exists);
    }

    @Test
    void findByEmail_success() {

        UserAccount user = UserAccount.builder()
                .email("find@test.com")
                .phone("999999")
                .passwordHash("hash")
                .role(UserAccount.Role.CLIENT)
                .status(UserAccount.Status.ACTIVE)
                .build();

        userRepository.save(user);

        UserAccount found = userRepository.findByEmail("find@test.com")
                .orElse(null);

        assertNotNull(found);

        assertEquals("999999", found.getPhone());
    }
}