package com.example.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderDTO {
    // 订单基本信息
    private List<OrderItemDTO> items;
    private BigDecimal amount;
    private BigDecimal discount;
    private BigDecimal actual;
    private String status;
    private String time;
    // 支付信息
    private String payType;
    private String payTime;
    // 物流信息
    private String address;
    private String phone;
    // 业务信息
    private Integer businessId;
    private Integer userId;
    private String user;
    // 其他信息
    private String comment;
    private String name;
    private String cover;
}