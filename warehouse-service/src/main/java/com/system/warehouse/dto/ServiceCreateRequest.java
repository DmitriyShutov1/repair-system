package com.system.warehouse.dto;

import com.system.warehouse.entity.ServiceCategory;

public record ServiceCreateRequest(
        String name,
        String serviceCode,
        ServiceCategory category
) {}