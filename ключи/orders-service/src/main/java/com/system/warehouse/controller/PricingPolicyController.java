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

    // =========================================================
    // UPSERT (создание новой версии цены)
    // =========================================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PricingPolicyResponse upsert(
            @RequestHeader("X-User-Role") String role,
            @RequestBody PricingPolicyUpsertRequest request
    ) {
        return pricingPolicyService.upsert(request);
    }

    // =========================================================
    // CLOSE CURRENT POLICY (PART)
    // =========================================================

    @DeleteMapping("/part/{partId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void closeForPart(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long partId
    ) {
        pricingPolicyService.closeCurrentPolicyForPart(partId);
    }

    // =========================================================
    // CLOSE CURRENT POLICY (SERVICE)
    // =========================================================

    @DeleteMapping("/service/{serviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void closeForService(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long serviceId
    ) {
        pricingPolicyService.closeCurrentPolicyForService(serviceId);
    }

    // =========================================================
    // HISTORY BY PART
    // =========================================================

    @GetMapping("/part/{partId}")
    public Page<PricingPolicyResponse> findAllByPart(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long partId,
            Pageable pageable
    ) {
        return pricingPolicyService.findAllByPartId(partId, pageable);
    }

    // =========================================================
    // HISTORY BY SERVICE
    // =========================================================

    @GetMapping("/service/{serviceId}")
    public Page<PricingPolicyResponse> findAllByService(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long serviceId,
            Pageable pageable
    ) {
        return pricingPolicyService.findAllByServiceId(serviceId, pageable);
    }

    // =========================================================
    // CURRENT ACTIVE (PART)
    // =========================================================

    @GetMapping("/part/{partId}/active")
    public PricingPolicyResponse findActiveByPart(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long partId
    ) {
        return pricingPolicyService.findActiveByPartId(partId);
    }

    // =========================================================
    // CURRENT ACTIVE (SERVICE)
    // =========================================================

    @GetMapping("/service/{serviceId}/active")
    public PricingPolicyResponse findActiveByService(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long serviceId
    ) {
        return pricingPolicyService.findActiveByServiceId(serviceId);
    }

    // =========================================================
    // ACTUAL AT MOMENT (PART)
    // =========================================================

    @GetMapping("/part/{partId}/at")
    public PricingPolicyResponse findActualAtMoment(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long partId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime moment
    ) {
        return pricingPolicyService.findActualAtMoment(partId, moment);
    }
}