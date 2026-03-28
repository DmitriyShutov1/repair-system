package com.system.users.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String address;

    private String phone;
}
