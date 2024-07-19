package com.example.OrderService.service;

import com.example.OrderService.feign.CatalogClient;
import com.example.OrderService.feign.DeliveryClient;
import com.example.OrderService.feign.PaymentClient;
import com.example.OrderService.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    @Autowired
    OrderRepository orderRepository;

    @Autowired
    PaymentClient paymentClient;

    @Autowired
    DeliveryClient deliveryClient;

    @Autowired
    CatalogClient catalogClient;
}
