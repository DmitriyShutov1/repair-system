package com.system.warehouse.controller;

import java.time.LocalDateTime;

import com.system.warehouse.dto.PricingPolicyResponse;
import com.system.warehouse.dto.PricingPolicyUpsertRequest;
import com.system.warehouse.service.PricingPolicyService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pricing-policies")
@RequiredArgsConstructor
public class PricingPolicyController {

    private final PricingPolicyService pricingPolicyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PricingPolicyResponse upsert(
            @RequestHeader("X-User-Role") String role,
            @RequestBody PricingPolicyUpsertRequest request
    ) {
    	if (!"ADMIN".equals(role)) 
    	    throw new IllegalStateException("You are not admin");
        return pricingPolicyService.upsert(request);
    }

    @DeleteMapping("/part/{partId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void closeForPart(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long partId
    ) {
    	if (!"ADMIN".equals(role)) 
    	    throw new IllegalStateException("You are not admin");
        pricingPolicyService.closeCurrentPolicyForPart(partId);
    }

    @DeleteMapping("/service/{serviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void closeForService(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long serviceId
    ) {
    	if (!"ADMIN".equals(role)) 
    	    throw new IllegalStateException("You are not admin");
        pricingPolicyService.closeCurrentPolicyForService(serviceId);
    }

    @GetMapping("/part/{partId}")
    public Page<PricingPolicyResponse> findAllByPart(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long partId,
            Pageable pageable
    ) {
    	if (!"ADMIN".equals(role)) 
    	    throw new IllegalStateException("You are not admin");
        return pricingPolicyService.findAllByPartId(partId, pageable);
    }

    @GetMapping("/service/{serviceId}")
    public Page<PricingPolicyResponse> findAllByService(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long serviceId,
            Pageable pageable
    ) {
    	if (!"ADMIN".equals(role)) 
    	    throw new IllegalStateException("You are not admin");
        return pricingPolicyService.findAllByServiceId(serviceId, pageable);
    }

    @GetMapping("/part/{partId}/active")
    public PricingPolicyResponse findActiveByPart(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long partId
    ) {
    	if (!"ADMIN".equals(role)) 
    	    throw new IllegalStateException("You are not admin");
        return pricingPolicyService.findActiveByPartId(partId);
    }

    @GetMapping("/service/{serviceId}/active")
    public PricingPolicyResponse findActiveByService(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long serviceId
    ) {
    	if (!"ADMIN".equals(role)) 
    	    throw new IllegalStateException("You are not admin");
        return pricingPolicyService.findActiveByServiceId(serviceId);
    }

    @GetMapping("/part/{partId}/at")
    public PricingPolicyResponse findActualAtMoment(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long partId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime moment
    ) {
    	if (!"ADMIN".equals(role)) 
    	    throw new IllegalStateException("You are not admin");
        return pricingPolicyService.findActualAtMoment(partId, moment);
    }
}