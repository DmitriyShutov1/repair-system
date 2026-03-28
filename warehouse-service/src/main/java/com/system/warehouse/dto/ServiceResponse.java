package com.system.warehouse.dto;

import com.system.warehouse.entity.ServiceCategory;

import lombok.Builder;

@Builder
public record ServiceResponse(
        Long id,
        String name,
        String serviceCode,
        ServiceCategory category,
        boolean active,
        Long version
) {}