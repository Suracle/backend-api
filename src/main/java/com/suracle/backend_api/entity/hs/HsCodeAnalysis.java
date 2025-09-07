package com.suracle.backend_api.entity.hs;

import com.suracle.backend_api.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "hs_code_analyses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HsCodeAnalysis extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "product_description", nullable = false, columnDefinition = "TEXT")
    private String productDescription;

    @Column(name = "suggested_hs_code", length = 20)
    private String suggestedHsCode;

    @Column(name = "hs_code_description", columnDefinition = "TEXT")
    private String hsCodeDescription;

    @Column(name = "confidence_score", precision = 3, scale = 2)
    private BigDecimal confidenceScore; // 0.00 ~ 1.00

    @Column(name = "reasoning", columnDefinition = "TEXT")
    private String reasoning; // 추천 근거

    @Column(name = "us_tariff_rate", precision = 5, scale = 4)
    private BigDecimal usTariffRate;

      @Column(name = "is_selected", nullable = false)
  @Builder.Default
  private Boolean isSelected = false;

    @Column(name = "analysis_session_id", length = 50)
    private String analysisSessionId; // 같은 분석 세션을 그룹화하기 위한 ID
}
