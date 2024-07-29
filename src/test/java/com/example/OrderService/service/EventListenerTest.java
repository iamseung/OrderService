package com.example.OrderService.service;

import com.example.OrderService.dto.DecreaseStockCountDto;
import com.example.OrderService.entity.ProductOrder;
import com.example.OrderService.enums.OrderStatus;
import com.example.OrderService.feign.CatalogClient;
import com.example.OrderService.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import payment.protobuf.EdaMessage;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import(EventListener.class)
public class EventListenerTest {

    @SpyBean
    OrderRepository orderRepository;

    @MockBean
    CatalogClient catalogClient;

    @MockBean
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    @Autowired
    EventListener eventListener;

    @Test
    void consumePaymentResultTest() throws Exception {
        // Given
        var productId = 111L;
        var paymentId = 222L;

        var productOrder = new ProductOrder(
                1L,
                productId,
                1L,
                OrderStatus.INITIATED,
                null,
                null,
                "서울시 마포구"
        );

        var order = orderRepository.save(productOrder);

        // 메세지 파싱
        var paymentResultMessage = EdaMessage.PaymentResultV1.newBuilder()
                .setOrderId(order.id) // PK
                .setPaymentId(paymentId)
                .setPaymentStatus("COMPLETED")
                .build();

        var catalogResponse = new HashMap<String, Object>();
        catalogResponse.put("name", "BIG TV");

        when(catalogClient.getProduct(productId)).thenReturn(catalogResponse);

        // When
        eventListener.consumePaymentResult(paymentResultMessage.toByteArray());

        // Then
        verify(kafkaTemplate, times(1)).send(
                eq("delivery_request"),
                any(byte[].class)
        );

        assertEquals(paymentId, order.paymentId);
    }

    @Captor
    ArgumentCaptor<DecreaseStockCountDto> captor;

    @Test
    @DisplayName("배송 상태 업데이트 메세지에 REQUESTED 가 들어 왔을 때 처리하는 과정 검증")
    void consumeDeliveryStatusUpdateTest_REQUEST() throws Exception {
        // Given
        var productId = 111L;
        var deliveryId = 222L;
        var productCount = 3L;

        var productOrder = new ProductOrder(
                1L,
                productId,
                productCount,
                OrderStatus.INITIATED,
                null,
                null,
                "서울시 마포구"
        );

        var order = orderRepository.save(productOrder);

        // 메세지 파싱
        var deliveryStatusUpdateMessage = EdaMessage.DeliveryStatusUpdateV1.newBuilder()
                .setOrderId(order.id) // PK
                .setDeliveryStatus("REQUESTED")
                .setDeliveryId(deliveryId)
                .build();

        // When
        eventListener.consumeDeliveryStatusUpdate(deliveryStatusUpdateMessage.toByteArray());

        // Then
        /*
            eventLister 에서 consumeDeliveryStatusUpdate 수행하고 난 후에, orderRepository 의 save 때문에
            deliveryId 가 저장된 것을 확인할 수 있음
         */
        assertEquals(deliveryId, order.deliveryId);

        verify(catalogClient, times(1)).decreaseStockCount(
                eq(productId),
                captor.capture() // 찍힌 값을 저장
        );

        assertEquals(productCount, captor.getValue().decreaseCount);
    }

    @Test
    @DisplayName("배송 상태 업데이트 메세지에 REQUESTED 가 아닌 것이 들어 왔을 때 아무 것도 처리하지 않음")
    void consumeDeliveryStatusUpdateTest_not_REQUEST() throws Exception {
        // Given
        // 메세지 파싱
        var deliveryStatusUpdateMessage = EdaMessage.DeliveryStatusUpdateV1.newBuilder()
                .setOrderId(1L) // PK
                .setDeliveryStatus("IN_DELIVERY")
                .setDeliveryId(10L)
                .build();

        // When
        eventListener.consumeDeliveryStatusUpdate(deliveryStatusUpdateMessage.toByteArray());

        // Then, 아무 동작하지 않음
        verify(orderRepository, times(0)).save(any());
        verify(catalogClient, times(0)).decreaseStockCount(any(),any());
    }
}
