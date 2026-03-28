package com.system.warehouse.dto;

import com.system.warehouse.entity.PartCategory;

public record PartCreateRequest(
        String name,
        String articleNumber,
        PartCategory category
) {}