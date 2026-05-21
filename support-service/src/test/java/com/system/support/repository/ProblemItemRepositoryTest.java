package com.system.support.repository;

import com.system.support.entity.*;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ProblemItemRepositoryTest {

    @Autowired
    private ProblemItemRepository repository;

    @Autowired
    private SupportRequestRepository supportRepository;

    @Test
    void findBySupportRequestId_success() {

        SupportRequest request = supportRepository.save(
                SupportRequest.builder()
                        .supportId(1L)
                        .branchId(1L)
                        .clientId(1L)
                        .orderId(100L)
                        .status(SupportRequestStatus.CREATED)
                        .description("Problem")
                        .deviceModel("Lenovo")
                        .deviceSerial("SN")
                        .build()
        );

        ProblemItem item = ProblemItem.builder()
                .supportRequest(request)
                .itemType("PART") 
                .name("SSD")
                .quantity(1)
                .sellPrice(BigDecimal.valueOf(5000))
                .build();

        repository.save(item);

        List<ProblemItem> result =
                repository.findBySupportRequestId(request.getId());

        assertEquals(1, result.size());
    }
}