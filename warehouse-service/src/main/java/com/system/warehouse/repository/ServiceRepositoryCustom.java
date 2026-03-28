package com.system.warehouse.repository;

import java.util.List;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.system.warehouse.dto.ServiceWithPriceDto;
import com.system.warehouse.entity.*;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;


public interface ServiceRepositoryCustom {

    Page<Service> searchByName(String query, Pageable pageable);

    void softDelete(Long id);

    Service update(Service service);
    
    Page<ServiceWithPriceDto> searchWithPriceByName(
            String query,
            Boolean active,
            Pageable pageable
    );

    Page<ServiceWithPriceDto> findWithPriceByCategory(
            ServiceCategory category,
            Boolean active,
            Pageable pageable
    );

    Optional<ServiceWithPriceDto> findWithPriceByServiceCode(
            String serviceCode,
            Boolean active
    );
    
    public Page<Service> findServicesWithoutActivePrice(Pageable pageable);
}