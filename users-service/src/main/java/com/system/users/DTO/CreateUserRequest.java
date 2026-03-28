package com.system.users.DTO;

import com.system.users.entity.UserAccount;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {

    // phone required in entity
    @NotBlank
    private String phone;

    // optional but if present must be valid format (simplified pattern)
    private String email;

    // optional role; if null — CLIENT
    private UserAccount.Role role;

    // optional branch id
    private Long branchId;
}
