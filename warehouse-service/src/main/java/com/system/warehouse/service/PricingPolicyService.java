package com.system.warehouse.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;


import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.system.warehouse.entity.*;
import com.system.warehouse.repository.PartRepository;
import com.system.warehouse.repository.PricingPolicyRepository;
import com.system.warehouse.repository.ServiceRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;

import com.system.warehouse.dto.*;
import lombok.RequiredArgsConstructor;

//ДОБАВЬ ИНДЕКС В МИГРАЦИЮ
//CREATE UNIQUE INDEX uniq_active_part_price
//ON pricing_policy(part_id)
//WHERE effective_to IS NULL;

//ВОТ ИХ ОБА НАДО
//CREATE UNIQUE INDEX uniq_active_part_price
//ON pricing_policy(part_id)
//WHERE effective_to IS NULL;
//
//CREATE UNIQUE INDEX uniq_active_service_price
//ON pricing_policy(service_id)
//WHERE effective_to IS NULL;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PricingPolicyService {

    private final PricingPolicyRepository pricingPolicyRepository;
    private final PartRepository partRepository;
    private final ServiceRepository serviceRepository;

    // =========================================================
    // UPSERT (создание новой политики с закрытием старой)
    // =========================================================

    @Transactional
    public PricingPolicyResponse upsert(PricingPolicyUpsertRequest request) {

        validateTarget(request);

        LocalDateTime now = LocalDateTime.now();

        // 1️⃣ Закрываем предыдущую активную политику
        if (request.partId() != null) {
            pricingPolicyRepository.findActiveByPartId(request.partId())
                    .ifPresent(existing -> {
                        existing.setEffectiveTo(now);
                    });
        }

        if (request.serviceId() != null) {
            pricingPolicyRepository.findActiveByServiceId(request.serviceId())
                    .ifPresent(existing -> {
                        existing.setEffectiveTo(now);
                    });
        }

        // 2️⃣ Загружаем ссылочную сущность
        Part part = null;
        com.system.warehouse.entity.Service service = null;

        if (request.partId() != null) {
            part = partRepository.findById(request.partId())
                    .orElseThrow(() -> new EntityNotFoundException("Part not found"));
        }

        if (request.serviceId() != null) {
            service = serviceRepository.findById(request.serviceId())
                    .orElseThrow(() -> new EntityNotFoundException("Service not found"));
        }

        // 3️⃣ Создаем новую запись
        PricingPolicy newPolicy = PricingPolicy.builder()
                .part(part)
                .service(service)
                .costPrice(request.costPrice())
                .clientPrice(request.clientPrice())
                .masterPercentage(request.masterPercentage())
                .effectiveFrom(now)
                .effectiveTo(null)
                .build();

        PricingPolicy saved = pricingPolicyRepository.save(newPolicy);

        return mapToResponse(saved);
    }

    // =========================================================
    // DELETE (закрытие текущей политики)
    // =========================================================

    @Transactional
    public void closeCurrentPolicyForPart(Long partId) {

        PricingPolicy policy = pricingPolicyRepository
                .findActiveByPartId(partId)
                .orElseThrow(() -> new EntityNotFoundException("Active pricing policy not found"));

        policy.setEffectiveTo(LocalDateTime.now());
    }

    @Transactional
    public void closeCurrentPolicyForService(Long serviceId) {

        PricingPolicy policy = pricingPolicyRepository
                .findActiveByServiceId(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Active pricing policy not found"));

        policy.setEffectiveTo(LocalDateTime.now());
    }

    // =========================================================
    // SEARCH BY PART
    // =========================================================

    public Page<PricingPolicyResponse> findAllByPartId(Long partId, Pageable pageable) {
        return pricingPolicyRepository
                .findAllByPartId(partId, pageable)
                .map(this::mapToResponse);
    }

    // =========================================================
    // SEARCH BY SERVICE
    // =========================================================

    public Page<PricingPolicyResponse> findAllByServiceId(Long serviceId, Pageable pageable) {
        return pricingPolicyRepository
                .findAllByServiceId(serviceId, pageable)
                .map(this::mapToResponse);
    }

    // =========================================================
    // CURRENT ACTIVE
    // =========================================================

    public PricingPolicyResponse findActiveByPartId(Long partId) {
        return pricingPolicyRepository.findActiveByPartId(partId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException("Active pricing policy not found"));
    }

    public PricingPolicyResponse findActiveByServiceId(Long serviceId) {
        return pricingPolicyRepository.findActiveByServiceId(serviceId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException("Active pricing policy not found"));
    }

    // =========================================================
    // ACTUAL AT MOMENT
    // =========================================================

    public PricingPolicyResponse findActualAtMoment(Long partId, LocalDateTime moment) {
        return pricingPolicyRepository.findActualAtMoment(partId, moment)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException("Pricing policy not found for moment"));
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateTarget(PricingPolicyUpsertRequest request) {

        if (request.partId() == null && request.serviceId() == null) {
            throw new IllegalArgumentException("Either partId or serviceId must be provided");
        }

        if (request.partId() != null && request.serviceId() != null) {
            throw new IllegalArgumentException("Only one of partId or serviceId must be provided");
        }
    }

    // =========================================================
    // MAPPER
    // =========================================================

    private PricingPolicyResponse mapToResponse(PricingPolicy policy) {
        return PricingPolicyResponse.builder()
                .id(policy.getId())
                .partId(policy.getPart() != null ? policy.getPart().getId() : null)
                .serviceId(policy.getService() != null ? policy.getService().getId() : null)
                .costPrice(policy.getCostPrice())
                .clientPrice(policy.getClientPrice())
                .masterPercentage(policy.getMasterPercentage())
                .effectiveFrom(policy.getEffectiveFrom())
                .effectiveTo(policy.getEffectiveTo())
                .version(policy.getVersion())
                .build();
    }
}