package com.system.warehouse.service;

import org.springframework.data.domain.Page;


import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.system.warehouse.entity.*;
import com.system.warehouse.repository.PartRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;

import com.system.warehouse.dto.*;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartService {

    private final PartRepository partRepository;

    @Transactional
    public PartResponse create(PartCreateRequest request) {

        if (partRepository.existsByArticleNumber(request.articleNumber())) {
            throw new IllegalStateException("Part with article number already exists");
        }

        Part part = Part.builder()
                .name(request.name())
                .articleNumber(request.articleNumber())
                .category(request.category())
                .active(true)
                .build();

        Part saved = partRepository.save(part);

        return mapToResponse(saved);
    }

    @Transactional
    public PartResponse update(Long id, PartUpdateRequest request) {

        Part part = partRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Part not found"));

        // optimistic locking check
        if (!part.getVersion().equals(request.version())) {
            throw new OptimisticLockException("Part was modified by another transaction");
        }

        part.setName(request.name());
        part.setCategory(request.category());

        if (request.active() != null) {
            part.setActive(request.active());
        }

        Part updated = partRepository.save(part);

        return mapToResponse(updated);
    }

    @Transactional
    public void delete(Long id) {

        Part part = partRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Part not found"));

        if (!part.isActive()) {
            return;
        }

        partRepository.softDelete(id);
    }

    public PartResponse findById(Long id) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Part not found"));

        return mapToResponse(part);
    }

    public PartResponse findByArticleNumber(String articleNumber) {
        Part part = partRepository.findByArticleNumber(articleNumber)
                .orElseThrow(() -> new EntityNotFoundException("Active part not found"));

        return mapToResponse(part);
    }

    public Page<PartResponse> findByCategory(PartCategory category, Pageable pageable, Boolean active) {
        return partRepository
                .findAllByCategoryAndActive(category, active, pageable)
                .map(this::mapToResponse);
    }

    public Page<PartResponse> searchByName(String query, Pageable pageable) {
        return partRepository
                .searchByName(query, pageable)
                .map(this::mapToResponse);
    }
    
    public Page<PartWithPriceAndStockDto> searchWithPriceAndStock(
            String query,
            Boolean active,
            Long branchId,
            Pageable pageable) {

        return partRepository.searchWithPriceAndStockByName(
                query,
                active,
                branchId,
                pageable
        );
    }
    
    public Page<PartWithPriceAndStockDto> findWithPriceAndStockByCategory(
            PartCategory category,
            Boolean active,
            Long branchId,
            Pageable pageable) {

        return partRepository.findWithPriceAndStockByCategory(
                category,
                active,
                branchId,
                pageable
        );
    }
    
    public PartWithPriceAndStockDto findWithPriceAndStockByArticle(
            String articleNumber,
            Boolean active,
            Long branchId) {

        return partRepository
                .findWithPriceAndStockByArticle(articleNumber, active, branchId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Part with price and stock not found"));
    }
    
    public Page<PartStockWaitingDto> findPartsWithStockAndWaiting(Long branchid, Pageable pageable){
    	return partRepository.findPartsWithStockAndWaiting(branchid, pageable);
    }
    
    public Page<Part> findPartsWithoutActivePrice(Pageable pageable) {
    	return partRepository.findPartsWithoutActivePrice(pageable);
    }
    
    public PartStockWaitingDto findPartWithStockAndWaitingByArticle(Long branchid,  String ArticleNumber) {
    	return partRepository.findPartWithStockAndWaitingByArticle(ArticleNumber, branchid).orElseThrow(() -> new EntityNotFoundException(
                "Part not found"));
    }
    
    private PartResponse mapToResponse(Part part) {
        return PartResponse.builder()
                .id(part.getId())
                .name(part.getName())
                .articleNumber(part.getArticleNumber())
                .category(part.getCategory())
                .active(part.isActive())
                .version(part.getVersion())
                .build();
    }
}