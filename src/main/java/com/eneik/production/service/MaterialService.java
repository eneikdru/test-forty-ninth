package com.eneik.production.service;

import com.eneik.production.dto.MaterialDto;
import com.eneik.production.models.persistence.MaterialEntity;
import com.eneik.production.repository.MaterialRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
