package com.system.warehouse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.system.warehouse.entity.*;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;


@Repository
public interface ServiceRepository extends JpaRepository<Service, Long>, ServiceRepositoryCustom {

    Optional<Service> findByServiceCode(String serviceCode);

    Page<Service> findAllByCategoryAndActive(ServiceCategory category, Pageable pageable, Boolean active);

    boolean existsByServiceCode(String serviceCode);
}