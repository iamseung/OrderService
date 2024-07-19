package com.example.OrderService.dto;

import java.util.Map;

public class StartOrderResponseDto {
    public Long orderId;
    // 유저가 선택할 수 있는 정보
    public Map<String, Object> paymentMethod;
    public Map<String, Object> address;
}
