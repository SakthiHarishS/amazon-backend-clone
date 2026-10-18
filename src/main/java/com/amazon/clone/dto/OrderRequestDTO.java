package com.amazon.clone.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequestDTO {
    private String customerName;
    private String userEmail;
    private String customerAddress;
    private String paymentMethod;
    private List<OrderItemDTO> items;
}