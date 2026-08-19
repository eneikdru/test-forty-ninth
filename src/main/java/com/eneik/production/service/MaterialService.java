package com.eneik.production.service;

import com.eneik.production.dto.MaterialDto;
import com.eneik.production.dto.MaterialUploadDto;
import com.eneik.production.dto.SearchEventRequestDTO;
import com.eneik.production.models.persistence.MaterialEntity;
import com.eneik.production.repository.MaterialRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final SearchAnalyticsService searchAnalyticsService;

    public MaterialService(MaterialRepository materialRepository, SearchAnalyticsService searchAnalyticsService) {
        this.materialRepository = materialRepository;
        this.searchAnalyticsService = searchAnalyticsService;
    }

    @Transactional
    public Page<MaterialDto> searchMaterials(String query, Pageable pageable) {
        long startTime = System.currentTimeMillis();
        Page<MaterialEntity> entities = materialRepository.searchMaterials(query, pageable);
        long executionTimeMs = System.currentTimeMillis() - startTime;

        if (searchAnalyticsService != null) {
            String sanitizedQuery = query != null ? query : "";
            SearchEventRequestDTO telemetryDTO = new SearchEventRequestDTO(
                    sanitizedQuery,
                    null,
                    null,
                    (int) entities.getTotalElements(),
                    executionTimeMs
            );
            try {
                searchAnalyticsService.recordSearchEvent(telemetryDTO);
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(MaterialService.class).warn("Failed to record search telemetry", e);
            }
        }

        return entities.map(MaterialDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Optional<MaterialEntity> getMaterialEntity(Long id) {
        return materialRepository.findById(id);
    }

    @Transactional
    public MaterialDto saveMaterial(MaterialEntity material) {
        MaterialEntity saved = materialRepository.save(material);
        return MaterialDto.fromEntity(saved);
    }

    @Transactional
    public MaterialDto createMaterial(MaterialUploadDto uploadDto) {
        MaterialEntity entity = new MaterialEntity();
        entity.setTitle(uploadDto.getTitle());
        entity.setDescription(uploadDto.getDescription());
        entity.setContent(uploadDto.getContent());
        entity.setCategory(uploadDto.getCategory());
        if (uploadDto.getTags() != null && !uploadDto.getTags().isEmpty()) {
            entity.setTags(String.join(",", uploadDto.getTags()));
        } else {
            entity.setTags(null);
        }

        MultipartFile file = uploadDto.getFile();
        if (file != null && !file.isEmpty()) {
            entity.setFileName(file.getOriginalFilename());
            entity.setContentType(file.getContentType());
            try {
                entity.setFileData(file.getBytes());
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to read uploaded file data", e);
            }
        }

        MaterialEntity saved = materialRepository.save(entity);
        return MaterialDto.fromEntity(saved);
    }

    @Transactional
    public Optional<MaterialDto> updateMaterial(Long id, MaterialUploadDto uploadDto) {
        Optional<MaterialEntity> optionalMaterial = materialRepository.findById(id);
        if (optionalMaterial.isEmpty()) {
            return Optional.empty();
        }

        MaterialEntity entity = optionalMaterial.get();
        entity.setTitle(uploadDto.getTitle());
        entity.setDescription(uploadDto.getDescription());
        entity.setContent(uploadDto.getContent());
        entity.setCategory(uploadDto.getCategory());
        if (uploadDto.getTags() != null && !uploadDto.getTags().isEmpty()) {
            entity.setTags(String.join(",", uploadDto.getTags()));
        } else {
            entity.setTags(null);
        }

        MultipartFile file = uploadDto.getFile();
        if (file != null && !file.isEmpty()) {
            entity.setFileName(file.getOriginalFilename());
            entity.setContentType(file.getContentType());
            try {
                entity.setFileData(file.getBytes());
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to read uploaded file data", e);
            }
        }

        MaterialEntity saved = materialRepository.save(entity);
        return Optional.of(MaterialDto.fromEntity(saved));
    }

    @Transactional
    public boolean deleteMaterial(Long id) {
        if (!materialRepository.existsById(id)) {
            return false;
        }
        materialRepository.deleteById(id);
        return true;
    }
}
