package com.system.warehouse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.warehouse.dto.PartCreateRequest;
import com.system.warehouse.dto.PartResponse;
import com.system.warehouse.entity.PartCategory;
import com.system.warehouse.service.PartService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PartController.class)
@AutoConfigureMockMvc(addFilters = false)
class PartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PartService partService;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void create_success() throws Exception {

        PartCreateRequest request =
                new PartCreateRequest(
                        "SSD",
                        "SSD-001",
                        PartCategory.SSD
                );

        PartResponse response = PartResponse.builder()
                .id(1L)
                .name("SSD")
                .articleNumber("SSD-001")
                .category(PartCategory.SSD)
                .active(true)
                .version(1L)
                .build();

        when(partService.create(request))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/parts")
                        .header("X-User-Role", "ADMIN")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("SSD"));
    }

    @Test
    void create_notAdmin() throws Exception {

        PartCreateRequest request =
                new PartCreateRequest(
                        "SSD",
                        "SSD-001",
                        PartCategory.SSD
                );

        mockMvc.perform(
                post("/api/parts")
                        .header("X-User-Role", "MASTER")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isConflict());
    }
}