package org.example.ordermanagement.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ordermanagement.db.entity.User;
import org.example.ordermanagement.db.entity.Order;
import org.example.ordermanagement.db.entity.Product;
import org.example.ordermanagement.db.enums.OrderStatus;
import org.example.ordermanagement.db.repository.UserRepository;
import org.example.ordermanagement.db.repository.OrderRepository;
import org.example.ordermanagement.db.repository.ProductRepository;
import org.example.ordermanagement.dto.request.OrderRequest;
import org.example.ordermanagement.dto.response.OrderDTO;
import org.example.ordermanagement.service.OrderService;
import org.example.ordermanagement.utils.EntityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository customerRepository;

    private static final Logger actionLogger = LoggerFactory.getLogger("org.example.ordermanagement.actions");

    @Override
    @Transactional
    @CachePut(value = "orders", key = "#result.orderId")
    public OrderDTO createOrder(OrderRequest orderRequest, Long customerId) {
        log.info("Creating order for customer ID: {}", customerId);
        actionLogger.info("Action: CreateOrder - CustomerID: {}", customerId);

        String customerName = findCustomerNameById(customerId);
        log.debug("Customer name resolved: {}", customerName);

        List<Product> products = findProductsByIds(orderRequest.getProductIds());
        log.debug("Products resolved: {}", products);

        BigDecimal totalPrice = calculateTotalPrice(products);
        log.debug("Total price calculated: {}", totalPrice);

        Order order = buildOrder(products, totalPrice, customerName);
        log.info("Order created with ID: {}", order.getId());
        actionLogger.info("Action: OrderCreated - OrderID: {}, CustomerID: {}", order.getId(), customerId);

        return OrderDTO.fromEntity(order);
    }

    private Order buildOrder(List<Product> products, BigDecimal totalPrice, String customerName) {
        Order order = new Order();
        order.setCustomerName(customerName);
        order.setProducts(products);
        order.setStatus(OrderStatus.Pending);
        order.setTotalPrice(totalPrice);
        order.setCreatedDate(LocalDateTime.now());
        return orderRepository.save(order);
    }


    @Override
    @Transactional
    @CacheEvict(value = "orders", key = "#orderId")
    public OrderDTO updateOrder(Long orderId, OrderRequest orderRequest, boolean isAdmin) {
        log.info("Updating order with ID: {}", orderId);
        actionLogger.info("Action: UpdateOrder - OrderID: {}, IsAdmin: {}", orderId, isAdmin);

        Order order = fetchOrderById(orderId);

        if (isAdmin) {
            updateOrderAsAdmin(order, orderRequest);
        } else {
            updateOrderAsClient(order, orderRequest);
        }

        order.setUpdatedDate(LocalDateTime.now());
        log.debug("Order fields updated for ID: {}", orderId);

        Order updatedOrder = orderRepository.save(order);
        log.info("Order updated successfully for ID: {}", orderId);
        actionLogger.info("Action: OrderUpdated - OrderID: {}, IsAdmin: {}", orderId, isAdmin);

        return OrderDTO.fromEntity(updatedOrder);
    }


    private void updateOrderAsAdmin(Order order, OrderRequest orderRequest) {
        EntityUtil.updateField(order::setCustomerName, findCustomerNameById(orderRequest.getCustomerId()));
        EntityUtil.updateField(order::setProducts, findProductsByIds(orderRequest.getProductIds()));
        EntityUtil.updateField(order::setTotalPrice, orderRequest.getTotalPrice());
        EntityUtil.updateField(order::setStatus, orderRequest.getStatus());
        log.debug("Admin updated fields for order ID: {}", order.getId());
    }

    private void updateOrderAsClient(Order order, OrderRequest orderRequest) {
        if (!OrderStatus.Cancelled.equals(orderRequest.getStatus())) {
            log.warn("RoleClient tried to update order ID {} to status {}", order.getId(), orderRequest.getStatus());
            throw new IllegalArgumentException("RoleClient can only update the status to 'Canceled'");
        }
        order.setStatus(OrderStatus.Cancelled);
        log.debug("Client updated status to 'Canceled' for order ID: {}", order.getId());
    }


    @Override
    @Cacheable(value = "orders", key = "#orderId")
    public OrderDTO getOrderById(Long orderId) {
        log.info("Fetching order by ID: {}", orderId);
        actionLogger.info("Action: GetOrderById - OrderID: {}", orderId);

        Order order = fetchOrderById(orderId);

        return OrderDTO.fromEntity(order);
    }

    @Override
    public Page<OrderDTO> getOrdersForAdmin(String status, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        log.info("Fetching orders for admin with filters - Status: {}, MinPrice: {}, MaxPrice: {}", status, minPrice, maxPrice);
        actionLogger.info("Action: GetOrdersForAdmin - Status: {}, MinPrice: {}, MaxPrice: {}", status, minPrice, maxPrice);

        return orderRepository.findFilteredOrders(status, minPrice, maxPrice, pageable)
                .map(OrderDTO::fromEntity);
    }

    @Override
    public Page<OrderDTO> getOrdersForCustomer(Long customerId, String status, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        String customerName = findCustomerNameById(customerId);
        log.info("Fetching orders for customer: {} with filters - Status: {}, MinPrice: {}, MaxPrice: {}", customerName, status, minPrice, maxPrice);
        actionLogger.info("Action: GetOrdersForCustomer - CustomerID: {}, Status: {}, MinPrice: {}, MaxPrice: {}", customerId, status, minPrice, maxPrice);

        return orderRepository.findFilteredOrdersForCustomer(customerName, status, minPrice, maxPrice, pageable)
                .map(OrderDTO::fromEntity);
    }

    @Override
    @Transactional
    @CacheEvict(value = "orders", key = "#orderId")
    public void softDeleteOrder(Long orderId) {
        log.info("Soft deleting order with ID: {}", orderId);
        actionLogger.info("Action: SoftDeleteOrder - OrderID: {}", orderId);

        Order order = fetchOrderById(orderId);

        order.setIsDeleted(true);
        order.setDeletedDate(LocalDateTime.now());
        orderRepository.save(order);

        log.info("Order soft deleted successfully for ID: {}", orderId);
        actionLogger.info("Action: OrderSoftDeleted - OrderID: {}", orderId);
    }

    private List<Product> findProductsByIds(List<Long> productIds) {
        List<Product> products = productRepository.findAllById(productIds);
        if (products.isEmpty() || products.size() != productIds.size()) {
            log.error("Some products not found for IDs: {}", productIds);
            throw new IllegalArgumentException("Some products were not found");
        }
        return products;
    }

    private BigDecimal calculateTotalPrice(List<Product> products) {
        return products.stream()
                .map(product -> product.getPrice().multiply(BigDecimal.valueOf(product.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String findCustomerNameById(Long customerId) {
        return customerRepository.findById(customerId)
                .map(User::getUsername)
                .orElseThrow(() -> {
                    log.error("Customer not found with ID: {}", customerId);
                    return new IllegalArgumentException("Customer not found with ID: " + customerId);
                });
    }


    private Order fetchOrderById(Long orderId) {
        return EntityUtil.findByIdOrThrow(
                orderRepository.findById(orderId),
                "Order not found with ID: {}",
                orderId
        );
    }
}
