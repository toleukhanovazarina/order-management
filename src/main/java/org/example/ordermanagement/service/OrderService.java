package org.example.ordermanagement.service;

import jakarta.transaction.Transactional;
import org.example.ordermanagement.dto.request.OrderRequest;
import org.example.ordermanagement.dto.response.OrderDTO;

public interface OrderService {
    @Transactional
    OrderDTO createOrder(OrderRequest orderRequest, Long customerId);
}
