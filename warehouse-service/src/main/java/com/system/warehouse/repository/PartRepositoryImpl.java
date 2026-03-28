package com.system.warehouse.repository;

import java.util.List;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.system.warehouse.dto.PartStockWaitingDto;
import com.system.warehouse.dto.PartWithPriceAndStockDto;
import com.system.warehouse.entity.*;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;


@Repository
@RequiredArgsConstructor
public class PartRepositoryImpl implements PartRepositoryCustom {

    private final EntityManager em;

    @Override
    public Page<Part> searchByName(String query, Pageable pageable) {

        String sql = """
            SELECT *
            FROM part
            WHERE name ILIKE :pattern
            ORDER BY similarity(name, :query) DESC
            """;

        List<Part> content = em.createNativeQuery(sql, Part.class)
                .setParameter("pattern", "%" + query + "%")
                .setParameter("query", query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        String countSql = """
            SELECT count(*)
            FROM part
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
                UPDATE Part p
                SET p.active = false,
                    p.version = p.version + 1
                WHERE p.id = :id
                """)
          .setParameter("id", id)
          .executeUpdate();
    }

    @Override
    public Part update(Part part) {
        return em.merge(part);
    }
    
    @Override
    public Page<PartWithPriceAndStockDto> searchWithPriceAndStockByName(
            String query,
            Boolean active,
            Long branchId,
            Pageable pageable) {

        String sql = """
            SELECT 
                p.id,
                p.name,
                p.article_number,
                p.category,
                p.is_active,
                pp.cost_price,
                pp.client_price,
        		pp.master_percentage,
                sb.quantity
            FROM part p
            JOIN pricing_policy pp 
                ON pp.part_id = p.id
               AND pp.effective_to IS NULL
            JOIN stock_balance sb 
                ON sb.part_id = p.id
               AND sb.branch_id = :branchId
            WHERE p.name ILIKE :pattern
              AND p.is_active = :active
            ORDER BY similarity(p.name, :query) DESC
            """;

        List<PartWithPriceAndStockDto> content =
                em.createNativeQuery(sql)
                  .setParameter("pattern", "%" + query + "%")
                  .setParameter("query", query)
                  .setParameter("branchId", branchId)
                  .setParameter("active", active)
                  .setFirstResult((int) pageable.getOffset())
                  .setMaxResults(pageable.getPageSize())
                  .unwrap(org.hibernate.query.NativeQuery.class)
                  .setTupleTransformer((tuple, aliases) ->
                          new PartWithPriceAndStockDto(
                                  ((Number) tuple[0]).longValue(),
                                  (String) tuple[1],
                                  (String) tuple[2],
                                  tuple[3].toString(),
                                  (Boolean) tuple[4],
                                  (java.math.BigDecimal) tuple[6],
                                  (java.math.BigDecimal) tuple[7],  // clientPrice
                                  (java.math.BigDecimal) tuple[5],  // masterPercentage
                                  ((Number) tuple[8]).intValue()
                          )
                  )
                  .getResultList();

        String countSql = """
            SELECT count(*)
            FROM part p
            JOIN pricing_policy pp 
                ON pp.part_id = p.id
               AND pp.effective_to IS NULL
            JOIN stock_balance sb 
                ON sb.part_id = p.id
               AND sb.branch_id = :branchId
            WHERE p.name ILIKE :pattern
              AND p.is_active = :active
            """;

        Long total = ((Number) em.createNativeQuery(countSql)
                .setParameter("pattern", "%" + query + "%")
                .setParameter("branchId", branchId)
                .setParameter("active", active)
                .getSingleResult()).longValue();

        return new PageImpl<>(content, pageable, total);
    }
    
    
    @Override
    public Page<PartWithPriceAndStockDto> findWithPriceAndStockByCategory(
            PartCategory category,
            Boolean active,
            Long branchId,
            Pageable pageable) {

        String sql = """
            SELECT 
                p.id,
                p.name,
                p.article_number,
                p.category,
                p.is_active,
                pp.cost_price,
                pp.client_price,
        		pp.master_percentage,
                sb.quantity
            FROM part p
            JOIN pricing_policy pp 
                ON pp.part_id = p.id
               AND pp.effective_to IS NULL
            JOIN stock_balance sb 
                ON sb.part_id = p.id
               AND sb.branch_id = :branchId
            WHERE p.category = :category
              AND p.is_active = :active
            """;

        List<PartWithPriceAndStockDto> content =
                em.createNativeQuery(sql)
                  .setParameter("category", category.name())
                  .setParameter("branchId", branchId)
                  .setParameter("active", active)
                  .setFirstResult((int) pageable.getOffset())
                  .setMaxResults(pageable.getPageSize())
                  .unwrap(org.hibernate.query.NativeQuery.class)
                  .setTupleTransformer((tuple, aliases) ->
                          new PartWithPriceAndStockDto(
                                  ((Number) tuple[0]).longValue(),
                                  (String) tuple[1],
                                  (String) tuple[2],
                                  tuple[3].toString(),
                                  (Boolean) tuple[4],
                                  (java.math.BigDecimal) tuple[6],
                                  (java.math.BigDecimal) tuple[7],  // clientPrice
                                  (java.math.BigDecimal) tuple[5],  // masterPercentage
                                  ((Number) tuple[8]).intValue()
                          )
                  )
                  .getResultList();

        return new PageImpl<>(content, pageable, content.size());
    }
    
    @Override
    public Optional<PartWithPriceAndStockDto> findWithPriceAndStockByArticle(
            String articleNumber,
            Boolean active,
            Long branchId) {

        String sql = """
            SELECT 
                p.id,
                p.name,
                p.article_number,
                p.category,
                p.is_active,
                pp.cost_price,
                pp.client_price,
        		pp.master_percentage,
                sb.quantity
            FROM part p
            JOIN pricing_policy pp 
                ON pp.part_id = p.id
               AND pp.effective_to IS NULL
            JOIN stock_balance sb 
                ON sb.part_id = p.id
               AND sb.branch_id = :branchId
            WHERE p.article_number = :articleNumber
              AND p.is_active = :active
            """;

        List<PartWithPriceAndStockDto> result =
                em.createNativeQuery(sql)
                  .setParameter("articleNumber", articleNumber)
                  .setParameter("branchId", branchId)
                  .setParameter("active", active)
                  .unwrap(org.hibernate.query.NativeQuery.class)
                  .setTupleTransformer((tuple, aliases) ->
                          new PartWithPriceAndStockDto(
                                  ((Number) tuple[0]).longValue(),
                                  (String) tuple[1],
                                  (String) tuple[2],
                                  tuple[3].toString(),
                                  (Boolean) tuple[4],
                                  (java.math.BigDecimal) tuple[6],
                                  (java.math.BigDecimal) tuple[7],  // clientPrice
                                  (java.math.BigDecimal) tuple[5],  // masterPercentage
                                  ((Number) tuple[8]).intValue()
                          )
                  )
                  .getResultList();

        return result.stream().findFirst();
    }
    
    @Override
    public Page<Part> findPartsWithoutActivePrice(Pageable pageable) {

        String sql = """
            SELECT p.*
            FROM part p
            LEFT JOIN pricing_policy pp
                   ON pp.part_id = p.id
                  AND pp.effective_to IS NULL
            WHERE pp.id IS NULL
            """;

        List<Part> content = em.createNativeQuery(sql, Part.class)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        String countSql = """
            SELECT count(*)
            FROM part p
            LEFT JOIN pricing_policy pp
                   ON pp.part_id = p.id
                  AND pp.effective_to IS NULL
            WHERE pp.id IS NULL
            """;

        Long total = ((Number) em.createNativeQuery(countSql)
                .getSingleResult()).longValue();

        return new PageImpl<>(content, pageable, total);
    }
    
    @Override
    public Page<PartStockWaitingDto> findPartsWithStockAndWaiting(
            Long branchId,
            Pageable pageable) {

        String sql = """
            SELECT
                p.id,
                p.name,
                p.article_number,
                p.category,
                p.is_active,
                sb.quantity,
                COALESCE(SUM(pwl.required_quantity),0) AS waiting_qty
            FROM part p
            JOIN stock_balance sb
                  ON sb.part_id = p.id
                 AND sb.branch_id = :branchId
            LEFT JOIN part_waiting_list pwl
                  ON pwl.part_id = p.id
                 AND pwl.branch_id = :branchId
                 AND pwl.is_closed = FALSE
            GROUP BY
                p.id,
                p.name,
                p.article_number,
                p.category,
                p.is_active,
                sb.quantity
            ORDER BY
                waiting_qty DESC,
                sb.quantity ASC
            """;

        List<PartStockWaitingDto> content =
                em.createNativeQuery(sql)
                  .setParameter("branchId", branchId)
                  .setFirstResult((int) pageable.getOffset())
                  .setMaxResults(pageable.getPageSize())
                  .unwrap(org.hibernate.query.NativeQuery.class)
                  .setTupleTransformer((tuple, aliases) ->
                          new PartStockWaitingDto(
                                  ((Number) tuple[0]).longValue(),
                                  (String) tuple[1],
                                  (String) tuple[2],
                                  tuple[3].toString(),
                                  (Boolean) tuple[4],
                                  ((Number) tuple[5]).intValue(),
                                  ((Number) tuple[6]).longValue()
                          )
                  )
                  .getResultList();

        String countSql = """
            SELECT count(*)
            FROM stock_balance sb
            WHERE sb.branch_id = :branchId
            """;

        Long total = ((Number) em.createNativeQuery(countSql)
                .setParameter("branchId", branchId)
                .getSingleResult()).longValue();

        return new PageImpl<>(content, pageable, total);
    }
    
    @Override
    public Optional<PartStockWaitingDto> findPartWithStockAndWaitingByArticle(
            String articleNumber,
            Long branchId) {

        String sql = """
            SELECT
                p.id,
                p.name,
                p.article_number,
                p.category,
                p.is_active,
                sb.quantity,
                COALESCE(SUM(pwl.required_quantity),0) AS waiting_qty
            FROM part p
            JOIN stock_balance sb
                  ON sb.part_id = p.id
                 AND sb.branch_id = :branchId
            LEFT JOIN part_waiting_list pwl
                  ON pwl.part_id = p.id
                 AND pwl.branch_id = :branchId
                 AND pwl.is_closed = FALSE
            WHERE p.article_number = :article
            GROUP BY
                p.id,
                p.name,
                p.article_number,
                p.category,
                p.is_active,
                sb.quantity
            """;

        List<PartStockWaitingDto> result =
                em.createNativeQuery(sql)
                  .setParameter("branchId", branchId)
                  .setParameter("article", articleNumber)
                  .unwrap(org.hibernate.query.NativeQuery.class)
                  .setTupleTransformer((tuple, aliases) ->
                          new PartStockWaitingDto(
                                  ((Number) tuple[0]).longValue(),
                                  (String) tuple[1],
                                  (String) tuple[2],
                                  tuple[3].toString(),
                                  (Boolean) tuple[4],
                                  ((Number) tuple[5]).intValue(),
                                  ((Number) tuple[6]).longValue()
                          )
                  )
                  .getResultList();

        return result.stream().findFirst();
    }
}