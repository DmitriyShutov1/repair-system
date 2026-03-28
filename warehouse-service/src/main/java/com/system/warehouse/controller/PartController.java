package com.system.warehouse.controller;

import com.system.warehouse.dto.PartCreateRequest;
import com.system.warehouse.dto.PartResponse;
import com.system.warehouse.dto.PartStockWaitingDto;
import com.system.warehouse.dto.PartUpdateRequest;
import com.system.warehouse.dto.PartWithPriceAndStockDto;
import com.system.warehouse.entity.Part;
import com.system.warehouse.entity.PartCategory;
import com.system.warehouse.service.PartService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
public class PartController {

    private final PartService partService;
    private final RestTemplate restTemplate = new RestTemplate();

    // =========================================================
    // CREATE
    // =========================================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PartResponse create(
            @RequestHeader("X-User-Role") String role,
            @RequestBody PartCreateRequest request
    ) {
    	if (!"ADMIN".equals(role)) 
    	    throw new IllegalStateException("You are not admin");
    	    
        return partService.create(request);
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @PutMapping("/{id}")
    public PartResponse update(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id,
            @RequestBody PartUpdateRequest request
    ) {
    	if (!"ADMIN".equals(role)) 
    	    throw new IllegalStateException("You are not admin");
        return partService.update(id, request);
    }

    // =========================================================
    // SOFT DELETE
    // =========================================================

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id
    ) {
    	if (!"ADMIN".equals(role)) 
    	    throw new IllegalStateException("You are not admin");
        partService.delete(id);
    }

    // =========================================================
    // FIND BY ID
    // =========================================================

    @GetMapping("/{id}")
    public PartResponse findById(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id
    ) {
    	if ("CLIENT".equals(role)) 
    	    throw new IllegalStateException("You are client");
        return partService.findById(id);
    }

    // =========================================================
    // FIND BY ARTICLE NUMBER
    // =========================================================

    @GetMapping("/by-article")
    public PartResponse findByArticleNumber(
            @RequestHeader("X-User-Role") String role,
            @RequestParam String articleNumber
    ) {
    	if ("CLIENT".equals(role)) 
    	    throw new IllegalStateException("You are client");
        return partService.findByArticleNumber(articleNumber);
    }

    // =========================================================
    // FIND BY CATEGORY (ACTIVE ONLY)
    // =========================================================

    @GetMapping("/by-category")
    public Page<PartResponse> findByCategory(
            @RequestHeader("X-User-Role") String role,
            @RequestParam PartCategory category,
            @RequestParam Boolean active,
            Pageable pageable
    ) {
    	if ("CLIENT".equals(role)) 
    	    throw new IllegalStateException("You are client");
        return partService.findByCategory(category, pageable, active);
    }

    // =========================================================
    // SEARCH BY NAME
    // =========================================================

    @GetMapping("/search")
    public Page<PartResponse> search(
            @RequestHeader("X-User-Role") String role,
            @RequestParam String query,
            Pageable pageable
    ) {
    	if ("CLIENT".equals(role)) 
    	    throw new IllegalStateException("You are client");
        return partService.searchByName(query, pageable);
    }
    
    @GetMapping("/search-with-stock")
    public Page<PartWithPriceAndStockDto> searchWithStock(
            @RequestHeader("X-User-Role") String role,
            @RequestParam String query,
            @RequestParam Boolean active,
            @RequestHeader("X-Branch-Id") Long branchId,
            Pageable pageable
    ) {
    	if ("CLIENT".equals(role)) 
    	    throw new IllegalStateException("You are client");
        return partService.searchWithPriceAndStock(
                query,
                active,
                branchId,
                pageable
        );
    }
    
    @GetMapping("/by-category-with-stock")
    public Page<PartWithPriceAndStockDto> findByCategoryWithStock(
            @RequestHeader("X-User-Role") String role,
            @RequestParam PartCategory category,
            @RequestParam Boolean active,
            @RequestHeader("X-Branch-Id") Long branchId,
            Pageable pageable
    ) {
    	if ("CLIENT".equals(role)) 
    	    throw new IllegalStateException("You are client");
        return partService.findWithPriceAndStockByCategory(
                category,
                active,
                branchId,
                pageable
        );
    }
    
    
    @GetMapping("/by-article-with-stock")
    public PartWithPriceAndStockDto findByArticleWithStock(
            @RequestHeader("X-User-Role") String role,
            @RequestParam String articleNumber,
            @RequestParam Boolean active,
            @RequestHeader("X-Branch-Id") Long branchId
    ) {
    	if ("CLIENT".equals(role)) 
    	    throw new IllegalStateException("You are client");
        return partService.findWithPriceAndStockByArticle(
                articleNumber,
                active,
                branchId
        );
    }
    
    @GetMapping("/pricelessParts")
    public Page<Part> findNotPriced(
            @RequestHeader("X-User-Role") String role,
            Pageable pageable
    ) {
        return partService.findPartsWithoutActivePrice(pageable);
    }
    
    @GetMapping("/withStockAndWaiting")
    public Page<PartStockWaitingDto> findPartsWithStockAndWaiting(
            @RequestHeader("X-User-Role") String role,
            Pageable pageable,
            @RequestHeader("X-Branch-Id") Long branchId
    ) {
        return partService.findPartsWithStockAndWaiting(branchId, pageable);
    }
    
    @GetMapping("/withStockAndWaitingByArticle")
    public PartStockWaitingDto findPartsWithStockAndWaitingByArticle(
            @RequestHeader("X-User-Role") String role,
            @RequestParam String articleNumber,
            @RequestHeader("X-Branch-Id") Long branchId
    ) {
        return partService.findPartWithStockAndWaitingByArticle(branchId, articleNumber);
    }
    
    @GetMapping("/test/ping-users")
    public String pingUsersService() {
        String response = restTemplate.getForObject(
            "http://users-service:8081/api/auth/test/hello", 
            String.class
        );
        return "Ответ от users сервиса: " + response;
    }
}