package com.example.OrderService.service;

import com.example.OrderService.dto.DecreaseStockCountDto;
import com.example.OrderService.enums.OrderStatus;
import com.example.OrderService.feign.CatalogClient;
import com.example.OrderService.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import payment.protobuf.EdaMessage;

// Bean 등록
@Component
public class EventListener {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    KafkaTemplate<String, byte[]> kafkaTemplate;

    @Autowired
    CatalogClient catalogClient;


    @KafkaListener(topics = "payment_result")
    public void consumePaymentResult(byte[] message) throws Exception {
        var object = EdaMessage.PaymentResultV1.parseFrom(message);
        logger.info("[payment_result] consumer : {}", object);

        // 결제 정보 업데이트
        var order = orderRepository.findById(object.getOrderId()).orElseThrow();
        order.paymentId = object.getPaymentId();
        order.orderStatus = OrderStatus.DELIVERY_REQUESTED;
        orderRepository.save(order);

        // 배송 요청
        var product = catalogClient.getProduct(order.productId);
        var deliveryRequest = EdaMessage.DeliveryRequestV1.newBuilder()
                    .setOrderId(order.id)
                    .setProductName(product.get("name").toString())
                    .setProductCount(order.count)
                    .setAddress(order.deliveryAddress)
                    .build();

        kafkaTemplate.send("delivery_request", deliveryRequest.toByteArray());
    }

    @KafkaListener(topics = "delivery_status_update")
    public void consumeDeliveryStatusUpdate(byte[] message) throws Exception {
        var object = EdaMessage.DeliveryStatusUpdateV1.parseFrom(message);
        logger.info("[delivery_status_update] consumer : {}", object);

        if(object.getDeliveryStatus().equals("REQUESTED")) {
            // 상품 재고 감소
            var order = orderRepository.findById(object.getOrderId()).orElseThrow();
            var decreaseStockCountDto = new DecreaseStockCountDto();
            decreaseStockCountDto.decreaseCount = order.count;
            catalogClient.decreaseStockCount(order.productId, decreaseStockCountDto);
        }
    }
}
