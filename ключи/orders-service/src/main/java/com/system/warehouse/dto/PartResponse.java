package com.system.warehouse.dto;

import com.system.warehouse.entity.PartCategory;


import lombok.Builder;

@Builder
public record PartResponse(
        Long id,
        String name,
        String articleNumber,
        PartCategory category,
        boolean active,
        Long version
) {}