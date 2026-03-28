package com.system.warehouse.controller;

import com.system.warehouse.dto.ServiceCreateRequest;
import com.system.warehouse.dto.ServiceResponse;
import com.system.warehouse.dto.ServiceUpdateRequest;
import com.system.warehouse.dto.ServiceWithPriceDto;
import com.system.warehouse.entity.ServiceCategory;
import com.system.warehouse.service.ServiceService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    // =========================================================
    // CREATE
    // =========================================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse create(
            @RequestHeader("X-User-Role") String role,
            @RequestBody ServiceCreateRequest request
    ) {
    	if (!"ADMIN".equals(role)) 
    	    throw new IllegalStateException("You are not admin");
        return serviceService.create(request);
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @PutMapping("/{id}")
    public ServiceResponse update(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id,
            @RequestBody ServiceUpdateRequest request
    ) {
    	if (!"ADMIN".equals(role)) 
    	    throw new IllegalStateException("You are not admin");
        return serviceService.update(id, request);
    }

    // =========================================================
    // SOFT DELETE
    // =========================================================

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id
    ) {
    	if(role != "ADMIN") {
    		throw new IllegalStateException("You are not admin");
    	}
        serviceService.delete(id);
    }

    // =========================================================
    // FIND BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ServiceResponse findById(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id
    ) {
    	if ("CLIENT".equals(role)) 
    	    throw new IllegalStateException("You are not client");
        return serviceService.findById(id);
    }

    // =========================================================
    // FIND BY SERVICE CODE
    // =========================================================

    @GetMapping("/by-code")
    public ServiceResponse findByServiceCode(
            @RequestHeader("X-User-Role") String role,
            @RequestParam String serviceCode
    ) {
    	if ("CLIENT".equals(role)) 
    	    throw new IllegalStateException("You are not client");
        return serviceService.findByServiceCode(serviceCode);
    }

    // =========================================================
    // FIND BY CATEGORY (ACTIVE ONLY)
    // =========================================================

    @GetMapping("/by-category")
    public Page<ServiceResponse> findByCategory(
            @RequestHeader("X-User-Role") String role,
            @RequestParam ServiceCategory category,
            @RequestParam Boolean active,
            Pageable pageable
    ) {
    	if ("CLIENT".equals(role)) 
    	    throw new IllegalStateException("You are not client");
        return serviceService.findByCategory(category, pageable, active);
    }

    // =========================================================
    // SEARCH BY NAME
    // =========================================================

    @GetMapping("/search")
    public Page<ServiceResponse> search(
            @RequestHeader("X-User-Role") String role,
            @RequestParam String query,
            Pageable pageable
    ) {
    	if ("CLIENT".equals(role)) 
    	    throw new IllegalStateException("You are not client");
        return serviceService.searchByName(query, pageable);
    }
    
 // =========================================================
 // SEARCH WITH PRICE
 // =========================================================

	 @GetMapping("/search-with-price")
	 public Page<ServiceWithPriceDto> searchWithPrice(
	         @RequestHeader("X-User-Role") String role,
	         @RequestParam String query,
	         @RequestParam Boolean active,
	         Pageable pageable
	 ) {
		 if ("CLIENT".equals(role)) 
	    	    throw new IllegalStateException("You are not client");
	     return serviceService.searchWithPriceByName(query, active, pageable);
	 }
    
	 
	// =========================================================
	// FIND BY CATEGORY WITH PRICE
	// =========================================================

	@GetMapping("/by-category-with-price")
	public Page<ServiceWithPriceDto> findByCategoryWithPrice(
	        @RequestHeader("X-User-Role") String role,
	        @RequestParam ServiceCategory category,
	        @RequestParam Boolean active,
	        Pageable pageable
	) {
		if ("CLIENT".equals(role)) 
    	    throw new IllegalStateException("You are not client");
	    return serviceService.findWithPriceByCategory(category, active, pageable);
	}
	
	
	@GetMapping("/pricelessServices")
	public Page<com.system.warehouse.entity.Service> findServicesWithoutActivePrice(
	        @RequestHeader("X-User-Role") String role,
	        Pageable pageable
	) {
		if ("CLIENT".equals(role)) 
    	    throw new IllegalStateException("You are not client");
	    return serviceService.findServicesWithoutActivePrice(pageable);
	}
	
	// =========================================================
	// FIND BY SERVICE CODE WITH PRICE
	// =========================================================

	@GetMapping("/by-code-with-price")
	public ServiceWithPriceDto findByCodeWithPrice(
	        @RequestHeader("X-User-Role") String role,
	        @RequestParam String serviceCode,
	        @RequestParam Boolean active
	) {
		if ("CLIENT".equals(role)) 
    	    throw new IllegalStateException("You are not client");
	    return serviceService.findWithPriceByServiceCode(serviceCode, active);
	}
    
}