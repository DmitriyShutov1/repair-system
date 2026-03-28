package com.system.warehouse.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.system.warehouse.dto.OrderBranchPair;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class OrdersClient {

    private final RestTemplate restTemplate;
    private static final String ORDERS_SERVICE_URL = "http://orders-service:8083";

    /**
     * Уведомляет orders-service о том, что заказы полностью обеспечены деталями
     * и могут быть переведены в статус IN_PROGRESS
     *
     * @param orderIds список идентификаторов заказов
     */
    public void notifyOrdersInProgress(Set<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return;
        }

        try {
            String url = ORDERS_SERVICE_URL + "/api/orders/set-in-progress";
            restTemplate.postForObject(url, orderIds, Void.class);
            
            System.out.println("Уведомление отправлено в orders-service для заказов: " + orderIds);
            
        } catch (Exception e) {
            System.err.println("Ошибка при вызове orders-service: " + e.getMessage());
            // Здесь можно добавить логирование или сохранение в таблицу для повторных попыток
            throw new RuntimeException("Failed to notify orders-service", e);
        }
    }
    
    public void cancelOrders(List<OrderBranchPair> pairs) {
        if (pairs == null || pairs.isEmpty()) {
            return;
        }

        try {
            String url = ORDERS_SERVICE_URL + "/api/orders/cancel-orders";
            restTemplate.postForObject(url, pairs, Void.class);
            
            System.out.println("Запрос на отмену отправлен в orders-service для заказов: " + pairs);
            
        } catch (Exception e) {
            System.err.println("Ошибка при вызове orders-service для отмены заказов: " + e.getMessage());
            throw new RuntimeException("Failed to cancel orders in orders-service", e);
        }
    }
}