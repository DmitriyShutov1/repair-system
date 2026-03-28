package com.system.warehouse.dto;

import java.math.BigDecimal;

public record PricingPolicyUpsertRequest(

        Long partId,
        Long serviceId,

        BigDecimal costPrice,
        BigDecimal clientPrice,
        BigDecimal masterPercentage
) {}