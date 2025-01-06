package org.example.ordermanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.ordermanagement.db.entity.Order;
import org.example.ordermanagement.db.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long orderId;
    private String customerName;
    private String status;
    private BigDecimal totalPrice;
    private List<String> products;
    private boolean isDeleted;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private LocalDateTime deletedDate;


    // Статический метод для преобразования из сущности в DTO
    public static OrderDTO fromEntity(Order order) {
        List<String> products = order.getProducts()
                                     .stream()
                                     .map(Product::getName)
                                     .toList();

        return new OrderDTO(
                order.getId(),
                order.getCustomerName(),
                order.getStatus().name(),
                order.getTotalPrice(),
                products,
                order.getIsDeleted(),
                order.getCreatedDate(),
                order.getUpdatedDate(),
                order.getDeletedDate()
        );
    }
}

