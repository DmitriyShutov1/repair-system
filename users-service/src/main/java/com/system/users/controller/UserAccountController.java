package com.system.users.controller;

import com.system.users.entity.UserAccount;


import com.system.users.service.UserAccountService;
import com.system.users.DTO.CreateUserRequest;
import com.system.users.DTO.ResetPasswordRequest;
import com.system.users.DTO.UpdateUserRequest;
import com.system.users.DTO.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/crud/users")
@RequiredArgsConstructor
@Validated
public class UserAccountController {

    private final UserAccountService userService;
    
    private void requireAdmin(String role) { 
    	if (role == null || !role.equals("ADMIN")) { 
    		throw new RuntimeException("Access denied: ADMIN role required"); 
    	}
    }
    
    private void requireMaster(String role) { 
    	if (role == null || !role.equals("MASTER")) { 
    		throw new RuntimeException("Access denied: MASTER role required"); 
    	}
    }
    
    private void requireMasterAdmin(String role) { 
    	if (role == null || (!role.equals("MASTER") && !role.equals("ADMIN"))) { 
    		throw new RuntimeException("Access denied: MASTER or ADMIN role required"); 
    	}
    }
    
    private void requireMasterAdminSupport(String role) { 
    	if (role == null || role.equals("CLIENT")) { 
    		throw new RuntimeException("Access denied: MASTER or ADMIN or SUPPORT role required"); 
    	}
    }
    
    @GetMapping("/by-email")
    public UserResponse getByEmail(
            @RequestHeader("X-User-Role") String role,
            @RequestParam String email
    ) {
        requireMasterAdminSupport(role);
        return UserResponse.fromEntity(userService.getByEmail(email));
    }
    
    @GetMapping("/by-phone")
    public UserResponse getByPhone(
            @RequestHeader("X-User-Role") String role,
            @RequestParam String phone
    ) {
        requireMasterAdminSupport(role);
        return UserResponse.fromEntity(userService.getByPhone(phone));
    }
    
    @GetMapping("/by-role")
    public Page<UserResponse> getByRole(
            @RequestHeader("X-User-Role") String roleHeader,
            @RequestParam UserAccount.Role role,
            @RequestParam UserAccount.Status status,
            Pageable pageable
    ) {
        requireAdmin(roleHeader);
        return userService.getByRoleAndStatus(role, status, pageable).map(UserResponse::fromEntity);
        
    }
    
    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getEmail());
    }

    @PostMapping("/createByAdmin") 
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@RequestHeader("X-User-Role") String role, @Valid @RequestBody CreateUserRequest request) {
    	requireAdmin(role); 
        UserAccount user = userService.createUser(request);
        return UserResponse.fromEntity(user);
    }
    
    @PostMapping("/createByMaster") 
    @ResponseStatus(HttpStatus.CREATED) 
    public UserResponse createClient( @RequestHeader("X-User-Role") String role, @Valid @RequestBody CreateUserRequest request ) { 
    	requireMaster(role);  
    	request.setRole(UserAccount.Role.CLIENT); 
    	UserAccount user = userService.createUser(request); 
    	return UserResponse.fromEntity(user); 
    }
    
    @GetMapping("/{id}")
    public UserResponse getById(@RequestHeader("X-User-Role") String role, @PathVariable("id") Long id) {
    	requireMasterAdminSupport(role); 
    	UserAccount user = userService.getById(id);
    	if(!role.equals("ADMIN") && user.getRole() != UserAccount.Role.CLIENT) {
    		throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to view this user");
    	}
        return UserResponse.fromEntity(userService.getById(id));
    }
    
    @GetMapping("/{id}/exists")
    public boolean userExists(@PathVariable Long id) {
    	return  userService.existsById(id, UserAccount.Role.CLIENT);
    }

    @GetMapping
    public Page<UserResponse> list(@RequestHeader("X-User-Role") String role, Pageable pageable) {
    	requireMasterAdmin(role); 
        return userService.getAll(pageable)
                .map(UserResponse::fromEntity);
    }

    @PatchMapping("/{id}")
    public UserResponse update(@RequestHeader("X-User-Role") String role, @PathVariable Long id, @RequestBody UpdateUserRequest request) {
    	requireAdmin(role); 
        return UserResponse.fromEntity(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader("X-User-Role") String role, @PathVariable Long id) {
    	requireAdmin(role); 
        userService.blockUser(id);
    }
}
