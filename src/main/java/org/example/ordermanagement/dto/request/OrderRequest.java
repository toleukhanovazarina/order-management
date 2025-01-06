package org.example.ordermanagement.dto.request;

import lombok.Data;
import org.example.ordermanagement.db.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequest {
    private Long customerId;
    private List<Long> productIds;
    private BigDecimal totalPrice;
    private OrderStatus status;
}
