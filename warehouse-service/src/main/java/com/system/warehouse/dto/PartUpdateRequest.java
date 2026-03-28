package com.system.warehouse.dto;

import com.system.warehouse.entity.PartCategory;

public record PartUpdateRequest(
        String name,
        PartCategory category,
        Boolean active,
        Long version
) {}