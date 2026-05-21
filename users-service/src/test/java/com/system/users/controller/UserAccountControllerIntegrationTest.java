package com.system.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.users.DTO.CreateUserRequest;
import com.system.users.entity.UserAccount;
import com.system.users.security.JwtAuthenticationFilter;
import com.system.users.security.JwtProperties;
import com.system.users.security.JwtService;
import com.system.users.service.MailService;
import com.system.users.service.UserAccountService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserAccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserAccountControllerIntegrationTest {

	@MockBean
	private JwtService jwtService;

	@MockBean
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	
	@MockBean
	private JwtProperties jwtProperties;
	
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private MailService mailService;

    @MockBean
    private UserAccountService userService;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void getById_success() throws Exception {

        UserAccount user = UserAccount.builder()
                .id(1L)
                .email("test@test.com")
                .phone("123456")
                .role(UserAccount.Role.CLIENT)
                .status(UserAccount.Status.ACTIVE)
                .build();

        when(userService.getById(1L))
                .thenReturn(user);

        mockMvc.perform(
                get("/api/crud/users/1")
                        .header("X-User-Role", "ADMIN")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email")
                        .value("test@test.com"));
    }

    @Test
    void createUser_success() throws Exception {

        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@test.com");
        request.setPhone("123456");
        request.setRole(UserAccount.Role.CLIENT);

        UserAccount user = UserAccount.builder()
                .id(1L)
                .email("test@test.com")
                .phone("123456")
                .role(UserAccount.Role.CLIENT)
                .status(UserAccount.Status.ACTIVE)
                .build();

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenReturn(user);

        mockMvc.perform(
                post("/api/crud/users/createByAdmin")
                        .header("X-User-Role", "ADMIN")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email")
                        .value("test@test.com"));
    }

    @Test
    void createUser_accessDenied() throws Exception {

        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@test.com");
        request.setPhone("123456");

        mockMvc.perform(
                post("/api/crud/users/createByAdmin")
                        .header("X-User-Role", "CLIENT")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().is5xxServerError());
    }

    @Test
    void deleteUser_success() throws Exception {

        mockMvc.perform(
                delete("/api/crud/users/1")
                        .header("X-User-Role", "ADMIN")
        )
                .andExpect(status().isNoContent());
    }
}