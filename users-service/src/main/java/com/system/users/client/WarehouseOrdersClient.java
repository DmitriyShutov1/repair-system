package com.system.users.client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class WarehouseOrdersClient {

    private final RestTemplate restTemplate;
    
    private static final String ORDERS_SERVICE_URL = "http://orders-service:8083";
    private static final String WAREHOUSE_SERVICE_URL = "http://warehouse-service:8082";

    // =============== ORDERS SERVICE METHODS ===============

    /**
     * Проверяет, есть ли у мастера активные заказы
     */
    public boolean hasMasterActiveOrders(Long masterId) {
        try {
            String url = ORDERS_SERVICE_URL + "/api/orders/master/" + masterId + "/has-active-orders";
            Boolean result = restTemplate.getForObject(url, Boolean.class);
            return result != null && result;
        } catch (Exception e) {
            log.error("Failed to check active orders for master {}: {}", masterId, e.getMessage());
            return false;
        }
    }

    // =============== WAREHOUSE SERVICE METHODS ===============

    /**
     * Удаляет все остатки по филиалу (требует роль ADMIN)
     */
    public void deleteAllStockByBranch(Long branchId) {
        try {
            String url = WAREHOUSE_SERVICE_URL + "/api/stock/branch/" + branchId;
            
            // Создаем заголовки с ролью
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Role", "ADMIN");
            
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            // Отправляем DELETE запрос с заголовками
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            
            log.info("Deleted all stock for branch: {}", branchId);
        } catch (Exception e) {
            log.error("Failed to delete stock for branch {}: {}", branchId, e.getMessage());
            throw new RuntimeException("Failed to delete warehouse stock for branch: " + branchId, e);
        }
    }
}