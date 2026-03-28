package com.system.users.DTO;

import com.system.users.entity.UserAccount;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class UserResponse {

    Long id;
    String email;
    String phone;
    UserAccount.Role role;
    UserAccount.Status status;
    Long branchId;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static UserResponse fromEntity(UserAccount user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
