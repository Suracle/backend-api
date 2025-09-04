package com.suracle.backend_api.dto.product;

import com.suracle.backend_api.entity.product.enums.ProductStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDto {

    @NotBlank(message = "상품명은 필수입니다")
    @Size(max = 255, message = "상품명은 255자를 초과할 수 없습니다")
    private String productName;

    @NotBlank(message = "상품 설명은 필수입니다")
    private String description;

    @NotNull(message = "가격은 필수입니다")
    @DecimalMin(value = "0.0", inclusive = false, message = "가격은 0보다 커야 합니다")
    @Digits(integer = 15, fraction = 4, message = "가격 형식이 올바르지 않습니다")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = false, message = "FOB 가격은 0보다 커야 합니다")
    @Digits(integer = 15, fraction = 4, message = "FOB 가격 형식이 올바르지 않습니다")
    private BigDecimal fobPrice;

    @Size(max = 3, message = "원산지 코드는 3자를 초과할 수 없습니다")
    private String originCountry;

    @Size(max = 20, message = "HS코드는 20자를 초과할 수 없습니다")
    private String hsCode;

    private ProductStatus status;

    @Builder.Default
    private Boolean isActive = true;
}
