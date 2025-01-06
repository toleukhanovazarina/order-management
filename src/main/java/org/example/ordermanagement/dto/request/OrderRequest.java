package org.example.ordermanagement.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private Long customerId;
    private List<Long> productIds;
}
