package com.example.OrderService.feign;

import com.example.OrderService.dto.DecreaseStockCountDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "catalogClient", url = "http://catalog-service:8080")
public interface CatalogClient {

    // 상품 조회
    @GetMapping("/catalog/products/{productId}")
    Map<String, Object> getProduct(@PathVariable Long productId);

    // 재고 감소
    @PostMapping("/catalog/products/{productId}/decreaseStockCount")
    void decreaseStockCount(@PathVariable Long productId, @RequestBody DecreaseStockCountDto dto);
}
