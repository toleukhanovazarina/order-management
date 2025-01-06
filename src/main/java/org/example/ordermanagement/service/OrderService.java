package org.example.ordermanagement.service;

import jakarta.transaction.Transactional;
import org.example.ordermanagement.dto.request.OrderRequest;
import org.example.ordermanagement.dto.response.OrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface OrderService {
    @Transactional
    OrderDTO createOrder(OrderRequest orderRequest, Long customerId);

    @Transactional
    OrderDTO updateOrder(Long orderId, OrderRequest orderRequest);

    // Получение заказов с фильтрацией для админа
    Page<OrderDTO> getOrdersForAdmin(String status, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // Получения заказов пользователя
    Page<OrderDTO> getOrdersForCustomer(Long customerId, String status, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    @Transactional
    void softDeleteOrder(Long orderId);
}
