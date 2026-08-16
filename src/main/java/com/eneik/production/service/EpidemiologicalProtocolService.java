package com.eneik.production.service;

import com.eneik.production.dto.EpidemiologicalProtocolDto;
import com.eneik.production.dto.EpidemiologicalProtocolRequestDto;
import com.eneik.production.models.persistence.EpidemiologicalProtocolEntity;
import com.eneik.production.repository.EpidemiologicalProtocolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EpidemiologicalProtocolService {

    private final EpidemiologicalProtocolRepository repository;

    public EpidemiologicalProtocolService(EpidemiologicalProtocolRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<EpidemiologicalProtocolDto> searchProtocols(String query, String category, String status) {
        List<EpidemiologicalProtocolEntity> entities;
        if (query != null && !query.trim().isEmpty()) {
            entities = repository.searchByKeyword(query.trim());
        } else {
            entities = repository.findAll();
        }

        return entities.stream()
                .filter(p -> category == null || category.trim().isEmpty() || category.equalsIgnoreCase(p.getCategory()))
                .filter(p -> status == null || status.trim().isEmpty() || status.equalsIgnoreCase(p.getStatus()))
                .map(EpidemiologicalProtocolDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<EpidemiologicalProtocolDto> getProtocolById(Long id) {
        return repository.findById(id).map(EpidemiologicalProtocolDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Optional<EpidemiologicalProtocolDto> getProtocolByCode(String code) {
        return repository.findByCode(code).map(EpidemiologicalProtocolDto::fromEntity);
    }

    public EpidemiologicalProtocolDto createProtocol(EpidemiologicalProtocolRequestDto requestDto) {
        if (requestDto == null) {
            throw new IllegalArgumentException("Request payload cannot be null");
        }
        if (requestDto.getCode() == null || requestDto.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Protocol code is required");
        }
        if (requestDto.getTitle() == null || requestDto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Protocol title is required");
        }
        if (repository.findByCode(requestDto.getCode()).isPresent()) {
            throw new IllegalArgumentException("Protocol with code " + requestDto.getCode() + " already exists");
        }

        EpidemiologicalProtocolEntity entity = new EpidemiologicalProtocolEntity(
                requestDto.getCode(),
                requestDto.getTitle(),
                requestDto.getCategory() != null ? requestDto.getCategory() : "General",
                requestDto.getVersion() != null ? requestDto.getVersion() : "v1.0",
                requestDto.getStatus() != null ? requestDto.getStatus() : "DRAFT",
                requestDto.getSummary(),
                requestDto.getAuthorOrganization() != null ? requestDto.getAuthorOrganization() : "Unknown",
                requestDto.getPublicationYear() != null ? requestDto.getPublicationYear() : 2026
        );

        EpidemiologicalProtocolEntity saved = repository.save(entity);
        return EpidemiologicalProtocolDto.fromEntity(saved);
    }

    public Optional<EpidemiologicalProtocolDto> updateProtocol(Long id, EpidemiologicalProtocolRequestDto requestDto) {
        Optional<EpidemiologicalProtocolEntity> optionalEntity = repository.findById(id);
        if (optionalEntity.isEmpty()) {
            return Optional.empty();
        }

        EpidemiologicalProtocolEntity entity = optionalEntity.get();
        if (requestDto.getTitle() != null && !requestDto.getTitle().trim().isEmpty()) {
            entity.setTitle(requestDto.getTitle().trim());
        }
        if (requestDto.getCategory() != null && !requestDto.getCategory().trim().isEmpty()) {
            entity.setCategory(requestDto.getCategory().trim());
        }
        if (requestDto.getVersion() != null && !requestDto.getVersion().trim().isEmpty()) {
            entity.setVersion(requestDto.getVersion().trim());
        }
        if (requestDto.getStatus() != null && !requestDto.getStatus().trim().isEmpty()) {
            entity.setStatus(requestDto.getStatus().trim());
        }
        if (requestDto.getSummary() != null) {
            entity.setSummary(requestDto.getSummary());
        }
        if (requestDto.getAuthorOrganization() != null && !requestDto.getAuthorOrganization().trim().isEmpty()) {
            entity.setAuthorOrganization(requestDto.getAuthorOrganization().trim());
        }
        if (requestDto.getPublicationYear() != null) {
            entity.setPublicationYear(requestDto.getPublicationYear());
        }

        EpidemiologicalProtocolEntity updated = repository.save(entity);
        return Optional.of(EpidemiologicalProtocolDto.fromEntity(updated));
    }

    public boolean deleteProtocol(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
