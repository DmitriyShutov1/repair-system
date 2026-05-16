package com.system.users.DTO;
import com.system.users.entity.UserAccount;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {
    private String email;
    private String phone;
    private Long branchId;
    private UserAccount.Role role;
    private UserAccount.Status status;
}
