package com.system.orders.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LatestTestResultDto {
    private Long testId;
    private String name;
    private String description;
    private boolean required;
    private boolean passed;
    private LocalDateTime testedAt;
}