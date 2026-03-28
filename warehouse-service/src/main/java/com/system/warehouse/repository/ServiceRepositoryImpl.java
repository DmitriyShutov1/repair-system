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
import java.math.BigDecimal;

@Repository
@RequiredArgsConstructor
public class ServiceRepositoryImpl implements ServiceRepositoryCustom {

    private final EntityManager em;

    @Override
    public Page<Service> searchByName(String query, Pageable pageable) {

        String sql = """
            SELECT *
            FROM service
            WHERE name ILIKE :pattern
            ORDER BY similarity(name, :query) DESC
            """;

        List<Service> content = em.createNativeQuery(sql, Service.class)
                .setParameter("pattern", "%" + query + "%")
                .setParameter("query", query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        String countSql = """
            SELECT count(*)
            FROM service
            WHERE name ILIKE :pattern
            """;

        Long total = ((Number) em.createNativeQuery(countSql)
                .setParameter("pattern", "%" + query + "%")
                .getSingleResult()).longValue();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public void softDelete(Long id) {
        em.createQuery("""
                UPDATE Service s
                SET s.active = false,
                    s.version = s.version + 1
                WHERE s.id = :id
                """)
          .setParameter("id", id)
          .executeUpdate();
    }

    @Override
    public Service update(Service service) {
        return em.merge(service);
    }
    
    @Override
    public Page<ServiceWithPriceDto> searchWithPriceByName(
            String query,
            Boolean active,
            Pageable pageable) {

        String sql = """
            SELECT
                s.id,
                s.name,
                s.service_code,
                s.category,
                s.is_active,
                pp.cost_price,
                pp.client_price,
                pp.master_percentage
            FROM service s
            JOIN pricing_policy pp
                ON pp.service_id = s.id
               AND pp.effective_to IS NULL
            WHERE s.name ILIKE :pattern
              AND s.is_active = :active
            ORDER BY similarity(s.name, :query) DESC
            """;

        List<ServiceWithPriceDto> content =
                em.createNativeQuery(sql)
                  .setParameter("pattern", "%" + query + "%")
                  .setParameter("query", query)
                  .setParameter("active", active)
                  .setFirstResult((int) pageable.getOffset())
                  .setMaxResults(pageable.getPageSize())
                  .unwrap(org.hibernate.query.NativeQuery.class)
                  .setTupleTransformer((tuple, aliases) ->
                          new ServiceWithPriceDto((
                                  (Number) tuple[0]).longValue(),
                                  (String) tuple[1],
                                  (String) tuple[2],
                                  tuple[3].toString(),
                                  (Boolean) tuple[4],
                                  (BigDecimal) tuple[5],
                                  (BigDecimal) tuple[6],             // client_price
                                  (BigDecimal) tuple[7]              // master_percentage
                          )
                  )
                  .getResultList();

        String countSql = """
            SELECT count(*)
            FROM service s
            JOIN pricing_policy pp
                ON pp.service_id = s.id
               AND pp.effective_to IS NULL
            WHERE s.name ILIKE :pattern
              AND s.is_active = :active
            """;

        Long total = ((Number) em.createNativeQuery(countSql)
                .setParameter("pattern", "%" + query + "%")
                .setParameter("active", active)
                .getSingleResult()).longValue();

        return new PageImpl<>(content, pageable, total);
    }
    
    @Override
    public Page<ServiceWithPriceDto> findWithPriceByCategory(
            ServiceCategory category,
            Boolean active,
            Pageable pageable) {

        String sql = """
            SELECT
                s.id,
                s.name,
                s.service_code,
                s.category,
                s.is_active,
                pp.cost_price,
                pp.client_price,
                pp.master_percentage
            FROM service s
            JOIN pricing_policy pp
                ON pp.service_id = s.id
               AND pp.effective_to IS NULL
            WHERE s.category = :category
              AND s.is_active = :active
            """;

        List<ServiceWithPriceDto> content =
                em.createNativeQuery(sql)
                  .setParameter("category", category.name())
                  .setParameter("active", active)
                  .setFirstResult((int) pageable.getOffset())
                  .setMaxResults(pageable.getPageSize())
                  .unwrap(org.hibernate.query.NativeQuery.class)
                  .setTupleTransformer((tuple, aliases) ->
                          new ServiceWithPriceDto(
                                  ((Number) tuple[0]).longValue(),
                                  (String) tuple[1],
                                  (String) tuple[2],
                                  tuple[3].toString(),
                                  (Boolean) tuple[4],
                                  (BigDecimal) tuple[5],
                                  (BigDecimal) tuple[6],             // client_price
                                  (BigDecimal) tuple[7]              // master_percentage
                          )
                  )
                  .getResultList();

        String countSql = """
            SELECT count(*)
            FROM service s
            JOIN pricing_policy pp
                ON pp.service_id = s.id
               AND pp.effective_to IS NULL
            WHERE s.category = :category
              AND s.is_active = :active
            """;

        Long total = ((Number) em.createNativeQuery(countSql)
                .setParameter("category", category.name())
                .setParameter("active", active)
                .getSingleResult()).longValue();

        return new PageImpl<>(content, pageable, total);
    }
    
    @Override
    public Optional<ServiceWithPriceDto> findWithPriceByServiceCode(
            String serviceCode,
            Boolean active) {

        String sql = """
            SELECT
                s.id,
                s.name,
                s.service_code,
                s.category,
                s.is_active,
                pp.cost_price,
                pp.client_price,
                pp.master_percentage
            FROM service s
            JOIN pricing_policy pp
                ON pp.service_id = s.id
               AND pp.effective_to IS NULL
            WHERE s.service_code = :serviceCode
              AND s.is_active = :active
            """;

        List<ServiceWithPriceDto> result =
                em.createNativeQuery(sql)
                  .setParameter("serviceCode", serviceCode)
                  .setParameter("active", active)
                  .unwrap(org.hibernate.query.NativeQuery.class)
                  .setTupleTransformer((tuple, aliases) ->
                          new ServiceWithPriceDto(
                                  ((Number) tuple[0]).longValue(),
                                  (String) tuple[1],
                                  (String) tuple[2],
                                  tuple[3].toString(),
                                  (Boolean) tuple[4],
                                  (BigDecimal) tuple[5],
                                  (BigDecimal) tuple[6],             // client_price
                                  (BigDecimal) tuple[7]              // master_percentage
                          )
                  )
                  .getResultList();

        return result.stream().findFirst();
    }
    
    @Override
    public Page<Service> findServicesWithoutActivePrice(Pageable pageable) {

        String sql = """
            SELECT s.*
            FROM service s
            LEFT JOIN pricing_policy pp
                   ON pp.service_id = s.id
                  AND pp.effective_to IS NULL
            WHERE pp.id IS NULL
            """;

        List<Service> content = em.createNativeQuery(sql, Service.class)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        String countSql = """
            SELECT count(*)
            FROM service s
            LEFT JOIN pricing_policy pp
                   ON pp.service_id = s.id
                  AND pp.effective_to IS NULL
            WHERE pp.id IS NULL
            """;

        Long total = ((Number) em.createNativeQuery(countSql)
                .getSingleResult()).longValue();

        return new PageImpl<>(content, pageable, total);
    }
}