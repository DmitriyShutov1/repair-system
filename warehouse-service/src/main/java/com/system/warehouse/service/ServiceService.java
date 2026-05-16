package com.system.warehouse.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.transaction.annotation.Transactional;

//import com.system.warehouse.entity.Service;
import com.system.warehouse.entity.ServiceCategory;

import org.springframework.stereotype.Service;
import com.system.warehouse.repository.PartRepository;
import com.system.warehouse.repository.ServiceRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;

import com.system.warehouse.dto.*;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceService {

    private final ServiceRepository serviceRepository;

    @Transactional
    public ServiceResponse create(ServiceCreateRequest request) {

        if (serviceRepository.existsByServiceCode(request.serviceCode())) {
            throw new IllegalStateException("Service with serviceCode already exists");
        }

        com.system.warehouse.entity.Service service = com.system.warehouse.entity.Service.builder()
                .name(request.name())
                .serviceCode(request.serviceCode())
                .category(request.category())
                .active(true)
                .build();

        com.system.warehouse.entity.Service saved = serviceRepository.save(service);

        return mapToResponse(saved);
    }

    @Transactional
    public ServiceResponse update(Long id, ServiceUpdateRequest request) {

    	com.system.warehouse.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        if (!service.getVersion().equals(request.version())) {
            throw new OptimisticLockException("Service was modified by another transaction");
        }

        service.setName(request.name());
        service.setCategory(request.category());

        if (request.active() != null) {
            service.setActive(request.active());
        }

        com.system.warehouse.entity.Service updated = serviceRepository.save(service);

        return mapToResponse(updated);
    }

    @Transactional
    public void delete(Long id) {

    	com.system.warehouse.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        if (!service.isActive()) {
            return;
        }

        serviceRepository.softDelete(id);
    }

    public ServiceResponse findById(Long id) {
    	com.system.warehouse.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        return mapToResponse(service);
    }

    public ServiceResponse findByServiceCode(String serviceCode) {
    	com.system.warehouse.entity.Service service = serviceRepository
                .findByServiceCode(serviceCode)
                .orElseThrow(() -> new EntityNotFoundException("Active service not found"));

        return mapToResponse(service);
    }

    public Page<ServiceResponse> findByCategory(ServiceCategory category, Pageable pageable, Boolean active) {
        return serviceRepository
                .findAllByCategoryAndActive(category, pageable, active)
                .map(this::mapToResponse);
    }

    public Page<ServiceResponse> searchByName(String query, Pageable pageable) {
        return serviceRepository
                .searchByName(query, pageable)
                .map(this::mapToResponse);
    }
    
    
	 public Page<ServiceWithPriceDto> searchWithPriceByName(
	         String query,
	         Boolean active,
	         Pageable pageable) {
	
	     return serviceRepository.searchWithPriceByName(query, active, pageable);
	 }
	
	 public Page<ServiceWithPriceDto> findWithPriceByCategory(
	         ServiceCategory category,
	         Boolean active,
	         Pageable pageable) {
	
	     return serviceRepository.findWithPriceByCategory(category, active, pageable);
	 }

	 public ServiceWithPriceDto findWithPriceByServiceCode(
	         String serviceCode,
	         Boolean active) {
	
	     return serviceRepository
	             .findWithPriceByServiceCode(serviceCode, active)
	             .orElseThrow(() -> new EntityNotFoundException("Service with price not found"));
	 }
	 
	 public Page<com.system.warehouse.entity.Service> findServicesWithoutActivePrice(Pageable pageable) {
		 return serviceRepository.findServicesWithoutActivePrice(pageable);
	 }

    private ServiceResponse mapToResponse(com.system.warehouse.entity.Service service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .serviceCode(service.getServiceCode())
                .category(service.getCategory())
                .active(service.isActive())
                .version(service.getVersion())
                .build();
    }
}