package com.system.users.DTO;

import com.system.users.entity.Branch;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class BranchResponse {

    Long id;
    String name;
    String address;
    String phone;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static BranchResponse fromEntity(Branch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .name(branch.getName())
                .address(branch.getAddress())
                .phone(branch.getPhone())
                .createdAt(branch.getCreatedAt())
                .updatedAt(branch.getUpdatedAt())
                .build();
    }
}
