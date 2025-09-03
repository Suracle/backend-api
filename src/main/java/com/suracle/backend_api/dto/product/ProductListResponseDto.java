package com.suracle.backend_api.dto.product;

import com.suracle.backend_api.entity.product.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListResponseDto {

    private Integer id;
    private Integer sellerId;
    private String sellerName;
    private String productId;
    private String productName;
    private BigDecimal price;
    private BigDecimal fobPrice;
    private String originCountry;
    private String hsCode;
    private ProductStatus status;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
