package com.suracle.backend_api.dto.requirement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 요구사항 분석 응답 DTO
 * 
 * AI Engine의 분석 결과를 Frontend로 전달합니다.
 * 
 * 포함 내용:
 * - 기본 요건 (criticalActions, requiredDocuments, complianceSteps)
 * - Phase 2-4 전문 분석 (검사절차, 처벌, 유효기간)
 * - 교차 검증 (규정 충돌 감지)
 * - 한국어 지원 (_ko 필드)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequirementAnalysisResponse {
    // 기본 정보
    private String productId;
    private String productName;
    private String hsCode;
    
    // 기존 필드 (하위 호환성)
    private List<Object> criticalActions;  // 객체 또는 문자열 지원
    private List<Object> requiredDocuments;  // 객체 또는 문자열 지원
    private List<Object> complianceSteps;  // 객체 또는 문자열 지원
    private Object timeline;  // 문자열 또는 객체 지원
    private String brokerRejectionReason;
    private String criticalDeadline;
    private String qualityStandards;
    private String coldChainRequirement;
    private String criticalWarning;
    private String pendingAnalysis;  // 오류 메시지 및 분석 상태 표시용
    private List<Object> sources;  // 객체 또는 문자열 지원
    
    // 메타 정보
    private double confidenceScore;
    private boolean isValid;
    private String lastUpdated;
    
    // Phase 2-4 전문 분석 결과 (신규 - 2025-10-11)
    private Object detailedRegulations;    // DetailedRegulationsResult
    private Object testingProcedures;      // TestingProceduresResult
    private Object penalties;              // PenaltiesResult
    private Object validity;               // ValidityResult
    private Object crossValidation;        // CrossValidationResult
    private Object precedentValidation;    // PrecedentValidationResult (판례 기반 검증)
    private Object overallConfidence;      // 통합 신뢰도 (판례 + 교차검증 + 출처)
    private Object verificationSummary;    // 검증 요약 (Frontend용 간단 버전)
    
    // 확장 필드 (신규 - 2025-10-12)
    private Object executionChecklist;     // ExecutionChecklist (pre/during/post import)
    private Object costBreakdown;          // CostBreakdown (mandatory/optional/hidden)
    private Object riskMatrix;             // RiskMatrix (high/medium risk)
    private Object complianceScore;        // ComplianceScore (overall + categories)
    private Object marketAccess;           // MarketAccess (retailer/state requirements)
    
    // LLM 확장 필드
    private Object estimatedCosts;         // EstimatedCosts (기존 + 확장)
    private List<Object> riskFactors;      // RiskFactors (확장)
    private List<Object> recommendations;  // Recommendations (확장)
    private Object timelineDetail;         // Timeline Detail
    private List<Object> labelingRequirements;        // 라벨링 요구사항
    private List<Object> testingRequirements;         // 테스트 요구사항
    private List<Object> prohibitedRestrictedSubstances;  // 금지/제한 물질
    private List<Object> priorNotifications;          // 사전 통지
    private List<Object> exemptions;                  // 면제 규정
}
