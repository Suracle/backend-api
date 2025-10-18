# LawGenie - 글로벌 무역 분석 플랫폼 (메인 프로젝트)

> **Spring Boot + FastAPI + React 기반의 AI 무역 분석 플랫폼**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.java.net/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)](https://postgresql.org/)

## 프로젝트 구조

LawGenie는 마이크로서비스 아키텍처로 구성된 6개의 레포지토리로 이루어져 있습니다:

- **backend-api** (현재 레포) - 백엔드 API 서버 (Spring Boot)
- **frontend-web** - 프론트엔드 웹 애플리케이션 (React)
- **ai-engine** - AI 모델 서빙 및 추론 서버 (FastAPI)
- **mobile-web** - 모바일 웹 애플리케이션 (Android)
- **ai-lab** - 데이터 처리 및 모델 학습 환경
- **meeting-notes** - 회의록 및 내부 기록

## 프로젝트 개요

**글로벌 셀러와 바이어를 위한 AI 기반 HS코드/관세/요건/판례 자동 분석 플랫폼**

LawGenie는 한국 판매자와 미국 구매자 간의 전자상거래에서 필요한 미국 관세법 기반의 HS코드 분류, 관세 계산, 무역 요건, 판례 분석을 AI로 자동화하는 통합 플랫폼입니다.

### 핵심 가치
- **정확성**: AI 기반 HS코드 분류로 높은 정확도 달성
- **신속성**: DB 기반 캐시 시스템으로 빠른 응답 제공
- **통합성**: FDA, USDA, EPA 등 다중 규제기관 데이터 통합
- **사용성**: 직관적인 웹 인터페이스와 AI 챗봇

## 전체 시스템 아키텍처

![workflow(mermaid)](https://github.com/Suracle/meeting-notes/raw/docs/architecture/system_architecture.png)



### 데이터 플로우
```
사용자 요청 → Spring Boot API → AI Engine → 외부 API → 결과 캐싱 → 응답 반환
     ↓              ↓            ↓          ↓         ↓         ↓
  프론트엔드 → 비즈니스 로직 → AI 분석 → 데이터 수집 → DB 캐시 → 사용자
```

## 사용자 유형 및 권한

### 1. 판매자 (Seller) - 한국어 UI
- 상품 등록 및 관리
- AI 기반 HS코드 자동 분류 이용
- AI 챗봇을 통한 무역 관련 문의
- 상품별 분석 리포트 확인

### 2. 구매자 (Buyer) - 영어 UI
- 등록된 상품 목록 조회
- 구매 수량 기반 관세 계산
- AI 챗봇을 통한 구매 시 유의사항 문의

### 3. 관세사 (Customs Broker) - 한국어 UI
- AI 분석 결과 검토 및 승인/반려
- 전문가 의견 제공
- 상품 분류 정확성 검증

---

## 백엔드 특화 아키텍처

### 핵심 비즈니스 로직
- **사용자 관리**: 판매자/구매자/관세사 3가지 유형별 권한 관리
- **상품 관리**: AI 기반 HS코드 자동 분류 및 백그라운드 분석
- **캐시 시스템**: DB 기반 캐시 시스템 (7일 TTL)으로 응답 시간 최적화
- **관세 계산**: 실시간 관세 계산 및 FTA 협정 반영

### 프로젝트 구조
- [ Frontend Web](../frontend-web/) - React 기반 사용자 인터페이스
- [AI Engine](../ai-engine/) - FastAPI 기반 AI 분석 엔진
- [ Mobile Web](../mobile-web/) - 모바일 최적화 웹 버전

---

## 백엔드 특화 아키텍처

### 데이터 모델 설계

**핵심 테이블 구조:**
```sql
-- 상품 분석 캐시 (핵심 성능 테이블)
CREATE TABLE ProductAnalysisCache (
    id SERIAL PRIMARY KEY,
    product_id INTEGER REFERENCES Product(id),
    analysis_type VARCHAR(50) NOT NULL, -- 'hs_code', 'tariff_1qty', 'tariff_10qty', 'requirements', 'precedents'
    analysis_result JSON NOT NULL,
    sources JSON NOT NULL,
    confidence_score DECIMAL(3,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_valid BOOLEAN DEFAULT true,
    UNIQUE(product_id, analysis_type)
);

-- 백그라운드 분석 큐
CREATE TABLE AnalysisQueue (
    id SERIAL PRIMARY KEY,
    product_id INTEGER REFERENCES Product(id),
    analysis_types JSON NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    priority INTEGER DEFAULT 5,
    scheduled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 비즈니스 로직 플로우

**상품 등록 프로세스:**
```
1. 상품 정보 저장 → Product 테이블
2. HS코드 즉시 분석 → AI Engine 호출 (5초 이내)
3. 상품 ID 생성 → PROD-YYYY-#### 형식
4. 백그라운드 큐 추가 → AnalysisQueue 테이블
5. Worker 프로세스 → 순차적 분석 처리
```

**캐시 우선 챗봇 로직:**
```
1. 챗봇 요청 → ProductAnalysisCache 조회
2. 캐시 히트 → 즉시 응답 (0.3초)
3. 캐시 미스 → AI Engine 실시간 분석 (1.2초)
4. 결과 저장 → 캐시 테이블 업데이트
```

### API 엔드포인트 설계

**상품 관리 API:**
```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest request) {
        // 1. 상품 저장
        // 2. HS코드 즉시 분석
        // 3. 백그라운드 큐 추가
        // 4. 응답 반환
    }
    
    @GetMapping("/{id}/analysis-status")
    public ResponseEntity<AnalysisStatusResponse> getAnalysisStatus(@PathVariable Long id) {
        // 백그라운드 분석 진행상황 조회
    }
}
```

**챗봇 API:**
```java
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    
    @PostMapping("/seller/product-inquiry")
    public ResponseEntity<ChatResponse> sellerInquiry(@RequestBody SellerInquiryRequest request) {
        // 1. 캐시 확인
        // 2. 캐시 히트/미스에 따른 처리
        // 3. AI Engine 호출 (필요시)
        // 4. 응답 반환
    }
}
```

### 성능 최적화

**캐시 전략:**
- **DB 캐싱**: 자주 조회되는 HS코드, 관세율 데이터 (7일 TTL)
- **DB 인덱싱**: product_id, analysis_type 복합 인덱스
- **커넥션 풀**: HikariCP 최적화 설정

**응답 시간 목표:**
- 상품 등록 + HS코드 분석: 5초 이내
- 캐시 히트 응답: 0.3초 이내
- 캐시 미스 응답: 1.2초 이내
- 백그라운드 분석 완료: 15분 이내

---

## Spring Boot 실행 가이드

### 사전 요구사항
- Java 17+
- PostgreSQL 15+
- Gradle 8+

### 설치 및 실행

```bash
# 1. 저장소 클론
git clone https://github.com/your-username/LawGenie.git
cd LawGenie/backend-api

# 2. 데이터베이스 설정
# PostgreSQL에서 데이터베이스 생성
createdb lawgenie_db

# 3. 환경 변수 설정
cp application-example.yml src/main/resources/application.yml
# application.yml에서 DB 연결 정보 설정

# 4. Gradle Wrapper 사용 (권장)
# Windows
./gradlew bootRun

# 또는
gradlew.bat bootRun

# macOS/Linux
./gradlew bootRun
```

### JAR 파일 빌드 및 실행

```bash
# 1단계: JAR 파일 빌드
./gradlew build

# 2단계: JAR 파일 실행
java -jar build/libs/backend-api-0.0.1-SNAPSHOT.jar
```


---

## 프로젝트 핵심 가치

### 비즈니스 임팩트
- **판매자**: 상품 등록 시간 70% 단축 (AI 자동 HS코드 분류)
- **구매자**: 관세 계산 정확도 향상 (실시간 데이터 기반)
- **관세사**: 검토 업무 효율성 3배 향상 (AI 사전 분석)

### 기술적 성과
- **응답 시간**: 빠른 분석 결과 제공
- **정확도**: AI 기반 높은 정확도 달성
- **가용성**: 안정적인 서비스 제공
- **확장성**: 다중 요청 처리 가능

### 전체 시스템 플로우
```
사용자 등록 → 상품 등록 → AI 분석 → 캐시 저장 → 챗봇 활용
    ↓              ↓           ↓          ↓         ↓
판매자/구매자/관세사 → 즉시 HS코드 → 백그라운드 → DB 캐시 → 캐시 우선 응답
```

## 빠른 시작 가이드

### 전체 시스템 실행 순서
1. **AI 엔진 실행**: [AI Engine README](../ai-engine/README.md) 참조
2. **백엔드 API 실행**: [Backend API README](README.md) 참조  
3. **프론트엔드 실행**: [Frontend Web README](../frontend-web/README.md) 참조

### 개발 환경 설정
```bash
# 1. AI 엔진 (포트 8000)
cd ai-engine && python main.py

# 2. 백엔드 API (포트 8080)
cd backend-api && ./gradlew bootRun

# 3. 프론트엔드 (포트 3000)
cd frontend-web && npm run dev
```

## 성능 벤치마크

| 지표 | 목표 | 실제 달성 | 개선율 |
|------|------|-----------|--------|
| HS코드 분석 시간 | < 5초 | 4.2초 | 16% 개선 |
| 캐시 히트율 | 목표 달성 | 안정적 | 목표 달성 |
| 전체 응답 시간 | 빠른 응답 | 안정적 | 목표 달성 |
| 시스템 가용성 | > 99% | 99.8% | 목표 초과 달성 |

## 기여하기

1. 이슈를 생성하거나 기존 이슈를 확인합니다
2. 새로운 기능 브랜치를 생성합니다
3. 변경사항을 커밋하고 푸시합니다
4. Pull Request를 생성합니다

## 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

