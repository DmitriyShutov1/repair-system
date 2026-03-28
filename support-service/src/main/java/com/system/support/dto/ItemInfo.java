package com.system.support.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.system.support.entity.SupportRequest;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemInfo {
	
    private Long id;

    private String itemType;

    private String name;

    private String category;

    private BigDecimal sellPrice;

    private Integer quantity;
    
    private Instant createdAt;
   
}