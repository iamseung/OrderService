package com.example.OrderService.dto;

import com.example.OrderService.enums.OrderStatus;

public class ProductOrderDetailDto {
    public Long id;
    public Long userId;
    public Long productId;
    public OrderStatus orderStatus;
    public Long paymentId;
    public Long deliveryId;
    public String paymentStatus; // 결제 상태
    public String deliveryStatus; // 배송 상태

    public ProductOrderDetailDto(Long id, Long userId, Long productId, OrderStatus orderStatus, Long paymentId, Long deliveryId, String paymentStatus, String deliveryStatus) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.orderStatus = orderStatus;
        this.paymentId = paymentId;
        this.deliveryId = deliveryId;
        this.paymentStatus = paymentStatus;
        this.deliveryStatus = deliveryStatus;
    }
}
