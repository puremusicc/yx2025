package com.example.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDTO {
    private Integer goodsId;
    private Integer quantity;
    private BigDecimal price;
}
