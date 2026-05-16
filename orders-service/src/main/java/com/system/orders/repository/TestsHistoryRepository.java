//package com.system.orders.repository;
//
//import com.system.orders.entity.TestsHistory;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface TestsHistoryRepository extends JpaRepository<TestsHistory, Long> {
//
//    List<TestsHistory> findByOrderIdOrderByTestedAtDesc(Long orderId);
//
//    @Query(value = """
//        SELECT th.* FROM tests_history th
//        JOIN (
//            SELECT test_id, MAX(tested_at) AS last_tested
//            FROM tests_history
//            WHERE order_id = :orderId
//            GROUP BY test_id
//        ) latest ON th.test_id = latest.test_id AND th.tested_at = latest.last_tested
//        WHERE th.order_id = :orderId
//    """, nativeQuery = true)
//    List<TestsHistory> findLatestByOrderId(@Param("orderId") Long orderId);
//    
//    @Query(value = """
//    	    SELECT th.id,
//    	           th.order_id,
//    	           th.test_id,
//    	           th.passed,
//    	           th.tested_at,
//    	           td.name,
//    	           td.description,
//    	           td.is_required
//    	    FROM tests_history th
//    	    JOIN (
//    	        SELECT test_id, MAX(tested_at) AS last_tested
//    	        FROM tests_history
//    	        WHERE order_id = :orderId
//    	        GROUP BY test_id
//    	    ) latest ON th.test_id = latest.test_id
//    	            AND th.tested_at = latest.last_tested
//    	    JOIN tests_description td ON td.id = th.test_id
//    	    WHERE th.order_id = :orderId
//    	    ORDER BY td.id
//    	    """, nativeQuery = true)
//    	List<Object[]> findLatestWithDescription(@Param("orderId") Long orderId);
//
//
//    boolean existsByOrderId(Long orderId);
//
//    void deleteByOrderId(Long orderId);
//}