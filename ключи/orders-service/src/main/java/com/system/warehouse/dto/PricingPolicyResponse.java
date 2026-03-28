package com.system.warehouse.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record PricingPolicyResponse(

        Long id,
        Long partId,
        Long serviceId,

        BigDecimal costPrice,
        BigDecimal clientPrice,
        BigDecimal masterPercentage,

        LocalDateTime effectiveFrom,
        LocalDateTime effectiveTo,

        Long version
) {}