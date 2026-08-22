package com.eneik.production.service;

import com.eneik.production.dto.CreateEpidemiologicalProtocolRequest;
import com.eneik.production.dto.EpidemiologicalProtocolDto;
import com.eneik.production.dto.EpidemiologicalProtocolSearchResult;
import com.eneik.production.dto.PaginationMeta;
import com.eneik.production.dto.UpdateEpidemiologicalProtocolRequest;
import com.eneik.production.models.persistence.EpidemiologicalProtocolEntity;
import com.eneik.production.repository.EpidemiologicalProtocolRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EpidemiologicalProtocolService {

    private final EpidemiologicalProtocolRepository repository;

    public EpidemiologicalProtocolService(EpidemiologicalProtocolRepository repository) {
        this.repository = repository;
    }

    public EpidemiologicalProtocolSearchResult searchProtocols(
            String q, String category, String status, String recordType, int page, int size, String sortBy, String sortOrder) {

        if (page < 0) {
            throw new IllegalArgumentException("Query parameter 'page' must be greater than or equal to 0");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Query parameter 'size' must be between 1 and 100");
        }

        String validSortBy = "createdAt";
        if (sortBy != null && !sortBy.isBlank()) {
            if ("createdAt".equalsIgnoreCase(sortBy) || "code".equalsIgnoreCase(sortBy) ||
                "title".equalsIgnoreCase(sortBy) || "publicationYear".equalsIgnoreCase(sortBy)) {
                validSortBy = sortBy;
                if ("publicationYear".equalsIgnoreCase(sortBy)) {
                    validSortBy = "publicationYear";
                }
            } else {
                throw new IllegalArgumentException("Invalid sort field: " + sortBy);
            }
        }

        Sort.Direction direction = Sort.Direction.DESC;
        if (sortOrder != null && "asc".equalsIgnoreCase(sortOrder)) {
            direction = Sort.Direction.ASC;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, validSortBy));

        Specification<EpidemiologicalProtocolEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (q != null && !q.isBlank()) {
                String pattern = "%" + q.toLowerCase().trim() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), pattern);
                Predicate codeLike = cb.like(cb.lower(root.get("code")), pattern);
                Predicate summaryLike = cb.like(cb.lower(root.get("summary")), pattern);
                Predicate authorLike = cb.like(cb.lower(root.get("authorOrganization")), pattern);
                predicates.add(cb.or(titleLike, codeLike, summaryLike, authorLike));
            }

            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase().trim()));
            }

            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("status")), status.toUpperCase().trim()));
            }

            if (recordType != null && !recordType.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("recordType")), recordType.toUpperCase().trim()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<EpidemiologicalProtocolEntity> entityPage = repository.findAll(spec, pageable);

        List<EpidemiologicalProtocolDto> dtos = entityPage.getContent().stream()
                .map(EpidemiologicalProtocolDto::fromEntity)
                .toList();

        PaginationMeta paginationMeta = new PaginationMeta(
                entityPage.getNumber(),
                entityPage.getSize(),
                entityPage.getTotalElements(),
                entityPage.getTotalPages(),
                entityPage.isFirst(),
                entityPage.isLast()
        );

        return new EpidemiologicalProtocolSearchResult(dtos, paginationMeta);
    }

    public Optional<EpidemiologicalProtocolDto> getProtocolById(Long id) {
        return repository.findById(id).map(EpidemiologicalProtocolDto::fromEntity);
    }

    @Transactional
    public EpidemiologicalProtocolDto createProtocol(CreateEpidemiologicalProtocolRequest request) {
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new IllegalArgumentException("Protocol code is required");
        }
        if (repository.findByCode(request.getCode()).isPresent()) {
            throw new DuplicateProtocolCodeException("Protocol with code '" + request.getCode() + "' already exists");
        }

        String normalizedRecordType = (request.getRecordType() != null && !request.getRecordType().isBlank())
                ? request.getRecordType().trim().toUpperCase()
                : "PROTOCOL";

        EpidemiologicalProtocolEntity entity = new EpidemiologicalProtocolEntity(
                request.getCode(),
                request.getTitle(),
                request.getCategory(),
                request.getVersion(),
                request.getStatus(),
                request.getSummary(),
                request.getAuthorOrganization(),
                request.getPublicationYear(),
                normalizedRecordType
        );

        EpidemiologicalProtocolEntity saved = repository.save(entity);
        return EpidemiologicalProtocolDto.fromEntity(saved);
    }

    @Transactional
    public EpidemiologicalProtocolDto updateProtocol(Long id, UpdateEpidemiologicalProtocolRequest request) {
        EpidemiologicalProtocolEntity existing = repository.findById(id)
                .orElseThrow(() -> new ProtocolNotFoundException("Epidemiological protocol with ID '" + id + "' was not found"));

        if (request.getCode() != null && !request.getCode().equals(existing.getCode())) {
            Optional<EpidemiologicalProtocolEntity> withSameCode = repository.findByCode(request.getCode());
            if (withSameCode.isPresent() && !withSameCode.get().getId().equals(id)) {
                throw new DuplicateProtocolCodeException("Protocol with code '" + request.getCode() + "' already exists");
            }
        }

        String expectedStatus = existing.getStatus();
        String newCode = request.getCode() != null ? request.getCode() : existing.getCode();
        String newTitle = request.getTitle() != null ? request.getTitle() : existing.getTitle();
        String newCategory = request.getCategory() != null ? request.getCategory() : existing.getCategory();
        String newVersion = request.getVersion() != null ? request.getVersion() : existing.getVersion();
        String newStatus = request.getStatus() != null ? request.getStatus() : existing.getStatus();
        String newSummary = request.getSummary() != null ? request.getSummary() : existing.getSummary();
        String newAuthorOrg = request.getAuthorOrganization() != null ? request.getAuthorOrganization() : existing.getAuthorOrganization();
        Integer newPubYear = request.getPublicationYear() != null ? request.getPublicationYear() : existing.getPublicationYear();
        String newRecordType = (request.getRecordType() != null && !request.getRecordType().isBlank())
                ? request.getRecordType().trim().toUpperCase()
                : existing.getRecordType();

        int rowsUpdated = repository.updateProtocolWithStatusGuard(
                id,
                expectedStatus,
                newCode,
                newTitle,
                newCategory,
                newVersion,
                newStatus,
                newSummary,
                newAuthorOrg,
                newPubYear,
                newRecordType
        );

        if (rowsUpdated == 0) {
            // Re-check if entity was deleted or status changed concurrently
            EpidemiologicalProtocolEntity rechecked = repository.findById(id)
                    .orElseThrow(() -> new ProtocolNotFoundException("Epidemiological protocol with ID '" + id + "' was not found"));
            throw new ConcurrentUpdateException("Protocol with ID '" + id + "' was modified concurrently (status changed from " + expectedStatus + " to " + rechecked.getStatus() + ")");
        }

        EpidemiologicalProtocolEntity updatedEntity = repository.findById(id)
                .orElseThrow(() -> new ProtocolNotFoundException("Epidemiological protocol with ID '" + id + "' was not found"));

        return EpidemiologicalProtocolDto.fromEntity(updatedEntity);
    }

    @Transactional
    public void deleteProtocol(Long id) {
        if (!repository.existsById(id)) {
            throw new ProtocolNotFoundException("Epidemiological protocol with ID '" + id + "' was not found");
        }
        repository.deleteById(id);
    }

    public static class DuplicateProtocolCodeException extends RuntimeException {
        public DuplicateProtocolCodeException(String message) {
            super(message);
        }
    }

    public static class ProtocolNotFoundException extends RuntimeException {
        public ProtocolNotFoundException(String message) {
            super(message);
        }
    }

    public static class ConcurrentUpdateException extends RuntimeException {
        public ConcurrentUpdateException(String message) {
            super(message);
        }
    }
}
