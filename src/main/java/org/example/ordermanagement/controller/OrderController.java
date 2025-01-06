package org.example.ordermanagement.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ordermanagement.dto.request.OrderRequest;
import org.example.ordermanagement.dto.response.OrderDTO;
import org.example.ordermanagement.service.OrderService;
import org.example.ordermanagement.utils.ReceiveToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final ReceiveToken receiveToken;

    @GetMapping
    public ResponseEntity<Page<OrderDTO>> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault Pageable pageable) {
        Map<String, String> userData = receiveToken.tokenData();
        String role = userData.get("role");

        try {
            Page<OrderDTO> orders;
            if ("RoleAdmin".equals(role)) {
                orders = orderService.getOrdersForAdmin(status, minPrice, maxPrice, pageable);
            } else if ("RoleClient".equals(role)) {
                Long customerId = Long.parseLong(userData.get("id"));
                orders = orderService.getOrdersForCustomer(customerId, status, minPrice, maxPrice, pageable);
            } else {
                log.warn("Unauthorized access attempt with role: {}", role);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(orders);
        } catch (Exception ex) {
            log.error("Error fetching orders: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
        Map<String, String> userData = receiveToken.tokenData();
        String role = userData.get("role");

        try {
            long customerId;
            if ("RoleAdmin".equals(role)) {
                if (request.getCustomerId() == null) {
                    log.warn("Customer ID is required for admin role");
                    return ResponseEntity.badRequest().body("Customer ID is required for admin");
                }
                customerId = request.getCustomerId();
            } else if ("RoleClient".equals(role)) {
                customerId = Long.parseLong(userData.get("id"));
            } else {
                log.warn("Unauthorized role trying to create order: {}", role);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            OrderDTO createdOrder = orderService.createOrder(request, customerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (Exception ex) {
            log.error("Error creating order: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Long orderId, @RequestBody OrderRequest request) {
        try {
            OrderDTO updatedOrder = orderService.updateOrder(orderId, request);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception ex) {
            log.error("Error updating order ID {}: {}", orderId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> softDeleteOrder(@PathVariable Long orderId) {
        Map<String, String> userData = receiveToken.tokenData();
        String role = userData.get("role");

        if (!"RoleAdmin".equals(role)) {
            log.warn("Unauthorized deletion attempt by role: {}", role);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            orderService.softDeleteOrder(orderId);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            log.error("Error deleting order ID {}: {}", orderId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
