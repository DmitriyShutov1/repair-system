package com.system.users.controller;

import com.system.users.service.BranchService; 
import com.system.users.DTO.BranchRequest;
import com.system.users.DTO.BranchResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crud/branches")
@RequiredArgsConstructor
@Validated
public class BranchController {

    private final BranchService branchService;
    
    private void requireAdmin(String role) { 
    	if (role == null || !role.equals("ADMIN")) { 
    		throw new RuntimeException("Access denied: ADMIN role required"); 
    	}
    }

    @PostMapping 
    @ResponseStatus(HttpStatus.CREATED) 
    public BranchResponse create(@RequestHeader("X-User-Role") String role, 
    		@Valid @RequestBody BranchRequest request ) { 
    	requireAdmin(role); 
    	return BranchResponse.fromEntity(branchService.createBranch(request)); 
    }
    

    @GetMapping("/{id}")
    public BranchResponse getById(@RequestHeader("X-User-Role") String role, @PathVariable Long id) {
    	requireAdmin(role); 
        return BranchResponse.fromEntity(branchService.getById(id));
    }

    @GetMapping
    public Page<BranchResponse> list(@RequestHeader("X-User-Role") String role, Pageable pageable) {
    	requireAdmin(role); 
        return branchService.getAll(pageable)
                .map(BranchResponse::fromEntity);
    }

    @PutMapping("/{id}")
    public BranchResponse update(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody BranchRequest request
    ) {
    	requireAdmin(role); 
        return BranchResponse.fromEntity(branchService.updateBranch(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
    		@RequestHeader("X-User-Role") String role,
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean force
    ) {
    	requireAdmin(role); 
        branchService.deleteBranch(id, force);
    }
}
