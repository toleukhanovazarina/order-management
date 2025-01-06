package org.example.ordermanagement.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.db.entity.Customer;
import org.example.ordermanagement.db.entity.Order;
import org.example.ordermanagement.db.entity.Product;
import org.example.ordermanagement.db.enums.OrderStatus;
import org.example.ordermanagement.db.repository.CustomerRepository;
import org.example.ordermanagement.db.repository.OrderRepository;
import org.example.ordermanagement.db.repository.ProductRepository;
import org.example.ordermanagement.dto.request.OrderRequest;
import org.example.ordermanagement.dto.response.OrderDTO;
import org.example.ordermanagement.service.OrderService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;


    @Override
    @Transactional
    public OrderDTO createOrder(OrderRequest orderRequest, Long customerId) {
        // Проверяем наличие клиента
        var customer = findCustomerById(customerId);

        // Получаем список продуктов
        List<Product> products = findProductsByIds(orderRequest.getProductIds());

        // Вычисляем общую стоимость заказа
        BigDecimal totalPrice = calculateTotalPrice(products);

        // Создаем и сохраняем заказ
        Order order = buildOrder(customer, products, totalPrice);

        // Возвращаем DTO
        return OrderDTO.fromEntity(order);
    }

    private Customer findCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + customerId));
    }

    private List<Product> findProductsByIds(List<Long> productIds) {
        List<Product> products = productRepository.findAllById(productIds);
        if (products.isEmpty() || products.size() != productIds.size()) {
            throw new IllegalArgumentException("Some products were not found");
        }
        return products;
    }

    private BigDecimal calculateTotalPrice(List<Product> products) {
        return products.stream()
                .map(product -> product.getPrice().multiply(BigDecimal.valueOf(product.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Order buildOrder(Customer customer, List<Product> products, BigDecimal totalPrice) {
        Order order = new Order();
        order.setCustomerName(customer.getUsername());
        order.setProducts(products);
        order.setStatus(OrderStatus.Pending);
        order.setTotalPrice(totalPrice);
        order.setCreatedDate(LocalDateTime.now());
        return orderRepository.save(order);
    }

}
