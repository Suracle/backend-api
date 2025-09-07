package com.suracle.backend_api.controller;

import com.suracle.backend_api.dto.product.ProductListResponseDto;
import com.suracle.backend_api.dto.product.ProductRequestDto;
import com.suracle.backend_api.dto.product.ProductResponseDto;
import com.suracle.backend_api.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 등록
     * @param productRequestDto 상품 등록 요청 정보
     * @param sellerId 판매자 ID (헤더 또는 파라미터로 전달)
     * @return 등록된 상품 정보
     */
    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(
            @Valid @RequestBody ProductRequestDto productRequestDto,
            @RequestHeader("X-Seller-Id") Integer sellerId) {
        try {
            log.info("상품 등록 요청 받음 - 판매자 ID: {}, 상품명: {}", sellerId, productRequestDto.getProductName());
            ProductResponseDto response = productService.createProduct(productRequestDto, sellerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("상품 등록 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("상품 등록 중 오류 발생 - 판매자 ID: {}, 상품명: {}, 오류: {}", 
                     sellerId, productRequestDto.getProductName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 상품 목록 조회 (페이징)
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @param sort 정렬 기준 (기본값: createdAt,desc)
     * @return 상품 목록
     */
    @GetMapping
    public ResponseEntity<Page<ProductListResponseDto>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        try {
            log.info("상품 목록 조회 요청 - 페이지: {}, 크기: {}, 정렬: {}", page, size, sort);
            
            String[] sortParams = sort.split(",");
            Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
            
            Page<ProductListResponseDto> products = productService.getProducts(pageable);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("상품 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 상품 상세 조회
     * @param productId 상품 ID
     * @return 상품 상세 정보
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable String productId) {
        try {
            log.info("상품 상세 조회 요청 - 상품 ID: {}", productId);
            ProductResponseDto product = productService.getProductById(productId);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            log.warn("상품 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("상품 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 상품 삭제
     * @param productId 상품 ID
     * @param sellerId 판매자 ID (헤더로 전달)
     * @return 삭제 결과
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable String productId,
            @RequestHeader("X-Seller-Id") Integer sellerId) {
        try {
            log.info("상품 삭제 요청 - 상품 ID: {}, 판매자 ID: {}", productId, sellerId);
            productService.deleteProduct(productId, sellerId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("상품 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("상품 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     * 상품명으로 검색
     * @param productName 상품명
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 검색된 상품 목록
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductListResponseDto>> searchProductsByName(
            @RequestParam String productName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("상품명 검색 요청 - 검색어: {}, 페이지: {}", productName, page);
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<ProductListResponseDto> products = productService.searchProductsByName(productName, pageable);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("상품 검색 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     * 판매자별 상품 검색 (상품명 + 상태 필터)
     * @param sellerId 판매자 ID
     * @param productName 상품명
     * @param status 상품 상태 (기본값: all)
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 검색된 상품 목록
     */
    @GetMapping("/seller/{sellerId}/search-filter")
    public ResponseEntity<Page<ProductListResponseDto>> searchProductsBySellerIdAndNameAndStatus(
            @PathVariable Integer sellerId,
            @RequestParam(required = false) String productName,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("판매자별 상품 검색 요청 (상품명 + 상태 필터) - 판매자 ID: {}, 검색어: {}, 상태: {}, 페이지: {}", sellerId, productName, status, page);
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<ProductListResponseDto> products = productService.searchProductsBySellerIdAndNameAndStatus(sellerId, productName, status, pageable);
            return ResponseEntity.ok(products);
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 상태 필터: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("판매자별 상품 검색 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
