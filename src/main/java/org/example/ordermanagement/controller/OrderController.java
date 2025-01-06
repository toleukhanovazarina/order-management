package org.example.ordermanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ordermanagement.dto.request.OrderRequest;
import org.example.ordermanagement.dto.response.OrderDTO;
import org.example.ordermanagement.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "API для управления заказами")
public class OrderController extends BaseController {

    private final OrderService orderService;

    @Operation(summary = "Получить список заказов", description = "Получение заказов с фильтрацией по статусу, цене и пагинацией.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешное получение заказов"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён для текущей роли"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping
    public ResponseEntity<Page<OrderDTO>> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault Pageable pageable) {
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

    @Operation(summary = "Получить заказ по ID", description = "Получение данных конкретного заказа по его идентификатору.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешное получение заказа"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён для текущей роли"),
            @ApiResponse(responseCode = "404", description = "Заказ не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long orderId) {
        try {
            OrderDTO order = orderService.getOrderById(orderId);
            return ResponseEntity.ok(order);
        } catch (Exception ex) {
            log.error("Error getting order ID {}: {}", orderId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Создать заказ", description = "Создание нового заказа.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Заказ успешно создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён для текущей роли"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
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

    @Operation(summary = "Обновить заказ", description = "Обновление данных существующего заказа.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Заказ успешно обновлён"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён для текущей роли"),
            @ApiResponse(responseCode = "404", description = "Заказ не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PutMapping("/{orderId}")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Long orderId, @RequestBody OrderRequest request) {
        String role = userData.get("role");

        try {
            OrderDTO updatedOrder;
            if ("RoleAdmin".equals(role)) {
                updatedOrder = orderService.updateOrder(orderId, request, true);
            } else if ("RoleClient".equals(role)) {
                updatedOrder = orderService.updateOrder(orderId, request, false);
            } else {
                log.warn("Unauthorized access attempt for updating order ID: {}", orderId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(updatedOrder);
        } catch (IllegalArgumentException ex) {
            log.error("Order not found for ID {}: {}", orderId, ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception ex) {
            log.error("Error updating order ID {}: {}", orderId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Удалить заказ", description = "Мягкое удаление заказа по его идентификатору.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Заказ успешно удалён"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён для текущей роли"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> softDeleteOrder(@PathVariable Long orderId) {
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
