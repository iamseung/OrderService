package com.example.OrderService.service;

import com.example.OrderService.entity.ProductOrder;
import com.example.OrderService.enums.OrderStatus;
import com.example.OrderService.feign.CatalogClient;
import com.example.OrderService.feign.DeliveryClient;
import com.example.OrderService.feign.PaymentClient;
import com.example.OrderService.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import(OrderService.class)
public class OrderServiceTest {

    // Test Target;
    @Autowired
    OrderService orderService;

    // @SpyBean, 실제 서비스를 사용하되, 일부만 Stubbing
    @SpyBean
    OrderRepository orderRepository;

    @MockBean
    PaymentClient paymentClient;

    @MockBean
    DeliveryClient deliveryClient;

    @MockBean
    CatalogClient catalogClient;

    @MockBean
    KafkaTemplate<String, byte[]> kafkaTemplate;

    @Test
    @DisplayName("주문 시작에 대한 테스트, order 에 대한 검증")
    void startOrderTest() {
        // Given
        var paymentMethodRes = new HashMap<String, Object>();
        var userAddressRes = new HashMap<String, Object>();
        paymentMethodRes.put("paymentMethodType", "CREDIT_CARD");
        userAddressRes.put("address", "서울시 마포구");

        when(paymentClient.getPaymentMethod(1L)).thenReturn(paymentMethodRes);
        when(deliveryClient.getUserAddress(1L)).thenReturn(userAddressRes);

        // When
        var StartOrderResponseDto = orderService.startOrder(1L, 1L, 2L);

        // Then
        assertNotNull(StartOrderResponseDto.orderId);
        assertEquals(paymentMethodRes, StartOrderResponseDto.paymentMethod);
        assertEquals(userAddressRes, StartOrderResponseDto.address);

        var order = orderRepository.findById(StartOrderResponseDto.orderId);
        assertEquals(OrderStatus.INITIATED, order.get().orderStatus);
    }

    @Test
    @DisplayName("주문 완료에 대한 테스트, order 에 대한 검증")
    void finishOrderTest() {
        // Given
        // startOrder 에 의존하지 않기 위해 직접 Repository 에 Insert
        var orderStarted = new ProductOrder(
                1L,
                1L,
                1L,
                OrderStatus.INITIATED,
                null,
                null,
                null
        );

        orderRepository.save(orderStarted);

        final var address = "서울시 마포구";

        var catalogResponse = new HashMap<String, Object>();
        var deliveryResponse = new HashMap<String, Object>();
        catalogResponse.put("price", "100");
        deliveryResponse.put("address", address);

        when(catalogClient.getProduct(orderStarted.productId)).thenReturn(catalogResponse);
        when(deliveryClient.getAddress(1L)).thenReturn(deliveryResponse);

        // When
        var response = orderService.finishOrder(orderStarted.id, 1L, 1L);

        // Then
        assertEquals(address, response.deliveryAddress);

        // kafka 가 1번 아래와 같은 메세지를 보냈는지 검증
        verify(kafkaTemplate, times(1)).send(
                eq("payment_request"),
                any(byte[].class)
        );
    }

    @Test
    @DisplayName("userId 로 조회했을 때, 주문에 대한 정보 검증")
    void getUserOrdersTest() {
        // Given
    	final var userId = 123L;

        var order1 = new ProductOrder(
                userId,
                100L,
                1L,
                OrderStatus.INITIATED,
                null,
                null,
                null
        );

        var order2 = new ProductOrder(
                userId,
                101L,
                1L,
                OrderStatus.INITIATED,
                null,
                null,
                null
        );

        orderRepository.save(order1);
        orderRepository.save(order2);

        // When
    	var response = orderService.getUserOrders(userId);

        // Then
        assertEquals(2, response.size());
        assertEquals(100L, response.get(0).productId);
        assertEquals(101L, response.get(1).productId);
    }

    @Test
    @DisplayName("주문 조회 상세 테스트")
    void getOrderDetailTest() {
        // Given
        var productOrder = new ProductOrder(
                1L,
                1L,
                1L,
                OrderStatus.INITIATED,
                10L,
                11L,
                null
        );

        orderRepository.save(productOrder);

        final var paymentStatus = "COMPLETED";
        final var deliveryStatus = "IN_DELIVERY";

        var paymentResponse = new HashMap<String, Object>();
        var deliveryResponse = new HashMap<String, Object>();
        paymentResponse.put("paymentStatus", paymentStatus);
        deliveryResponse.put("deliveryStatus", deliveryStatus);

        when(orderRepository.findById(1000L)).thenReturn(Optional.of(productOrder));
        when(paymentClient.getPayment(10L)).thenReturn(paymentResponse);
        when(deliveryClient.getDelivery(11L)).thenReturn(deliveryResponse);

        // When
        var response = orderService.getOrderDetail(1000L);

        // Then
        assertEquals(10L, response.paymentId);
        assertEquals(11L, response.deliveryId);
        assertEquals(paymentStatus, response.paymentStatus);
        assertEquals(deliveryStatus, response.deliveryStatus);
    }
}
