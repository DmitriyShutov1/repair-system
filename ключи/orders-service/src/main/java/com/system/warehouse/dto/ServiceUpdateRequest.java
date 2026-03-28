package com.system.warehouse.dto;

import com.system.warehouse.entity.ServiceCategory;

public record ServiceUpdateRequest(
        String name,
        ServiceCategory category,
        Boolean active,
        Long version
) {}