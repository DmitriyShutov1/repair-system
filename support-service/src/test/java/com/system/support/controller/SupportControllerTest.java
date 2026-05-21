package com.system.support.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.support.controler.SupportController;
import com.system.support.entity.SupportRequest;
import com.system.support.entity.SupportRequestStatus;
import com.system.support.service.SupportService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SupportController.class)
@AutoConfigureMockMvc(addFilters = false)
class SupportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SupportService supportService;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void requireReturn_success() throws Exception {

        SupportRequest request = SupportRequest.builder()
                .id(1L)
                .status(SupportRequestStatus.RETURN_REQUIRED)
                .createdAt(Instant.now())
                .build();

        when(supportService.requireReturn(1L))
                .thenReturn(request);

        mockMvc.perform(
                post("/api/support-requests/1/require-return")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status")
                        .value("RETURN_REQUIRED"));
    }

    @Test
    void requireReturn_wrongStatus() throws Exception {

        when(supportService.requireReturn(1L))
                .thenThrow(
                        new IllegalStateException(
                                "Return not allowed in current status"
                        )
                );

        mockMvc.perform(
                post("/api/support-requests/1/require-return")
        )
                .andExpect(status().isConflict());
    }
}