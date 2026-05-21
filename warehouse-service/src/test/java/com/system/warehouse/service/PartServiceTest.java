package com.system.warehouse.service;

import com.system.warehouse.dto.PartCreateRequest;
import com.system.warehouse.dto.PartResponse;
import com.system.warehouse.dto.PartUpdateRequest;
import com.system.warehouse.entity.Part;
import com.system.warehouse.entity.PartCategory;
import com.system.warehouse.repository.PartRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartServiceTest {

    @Mock
    private PartRepository partRepository;

    @InjectMocks
    private PartService partService;

    private Part part;

    @BeforeEach
    void setup() {
        part = Part.builder()
                .id(1L)
                .name("SSD Samsung")
                .articleNumber("SSD-001")
                .category(PartCategory.SSD)
                .active(true)
                .version(1L)
                .build();
    }

    @Test
    void create_success() {

        PartCreateRequest request = new PartCreateRequest(
                "SSD Samsung",
                "SSD-001",
                PartCategory.SSD
        );

        when(partRepository.existsByArticleNumber("SSD-001"))
                .thenReturn(false);

        when(partRepository.save(any(Part.class)))
                .thenReturn(part);

        PartResponse response = partService.create(request);

        assertEquals("SSD Samsung", response.name());
        assertEquals("SSD-001", response.articleNumber());

        verify(partRepository).save(any(Part.class));
    }

    @Test
    void create_duplicateArticle() {

        PartCreateRequest request = new PartCreateRequest(
                "SSD Samsung",
                "SSD-001",
                PartCategory.SSD
        );

        when(partRepository.existsByArticleNumber("SSD-001"))
                .thenReturn(true);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> partService.create(request)
        );

        assertEquals(
                "Part with article number already exists",
                ex.getMessage()
        );

        verify(partRepository, never()).save(any());
    }

    @Test
    void update_success() {

        PartUpdateRequest request = new PartUpdateRequest(
                "SSD Kingston",
                PartCategory.SSD,
                true,
                1L
        );

        when(partRepository.findById(1L))
                .thenReturn(Optional.of(part));

        when(partRepository.save(any(Part.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PartResponse response = partService.update(1L, request);

        assertEquals("SSD Kingston", response.name());

        verify(partRepository).save(part);
    }

    @Test
    void update_wrongVersion() {

        PartUpdateRequest request = new PartUpdateRequest(
                "SSD Kingston",
                PartCategory.SSD,
                true,
                999L
        );

        when(partRepository.findById(1L))
                .thenReturn(Optional.of(part));

        assertThrows(
                OptimisticLockException.class,
                () -> partService.update(1L, request)
        );

        verify(partRepository, never()).save(any());
    }

    @Test
    void findById_notFound() {

        when(partRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> partService.findById(1L)
        );
    }

    @Test
    void delete_success() {

        when(partRepository.findById(1L))
                .thenReturn(Optional.of(part));

        partService.delete(1L);

        verify(partRepository).softDelete(1L);
    }
}