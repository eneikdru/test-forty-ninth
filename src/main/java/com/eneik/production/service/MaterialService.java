package com.eneik.production.service;

import com.eneik.production.dto.MaterialDto;
import com.eneik.production.dto.MaterialUploadDto;
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

    public MaterialService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Transactional(readOnly = true)
    public Page<MaterialDto> searchMaterials(String query, Pageable pageable) {
        Page<MaterialEntity> entities = materialRepository.searchMaterials(query, pageable);
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
}
