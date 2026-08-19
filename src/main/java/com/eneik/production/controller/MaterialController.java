package com.eneik.production.controller;

import com.eneik.production.dto.ErrorResponse;
import com.eneik.production.dto.FieldErrorDto;
import com.eneik.production.dto.MaterialDto;
import com.eneik.production.dto.MaterialUploadDto;
import com.eneik.production.models.persistence.MaterialEntity;
import com.eneik.production.service.MaterialService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/materials")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @GetMapping("/search")
    public ResponseEntity<Page<MaterialDto>> searchMaterials(
            @RequestParam(name = "query", required = false) String query,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<MaterialDto> results = materialService.searchMaterials(query, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable("id") Long id) {
        Optional<MaterialEntity> optionalMaterial = materialService.getMaterialEntity(id);
        if (optionalMaterial.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        MaterialEntity material = optionalMaterial.get();
        byte[] fileData = material.getFileData() != null ? material.getFileData() : new byte[0];
        ByteArrayResource resource = new ByteArrayResource(fileData);

        String contentTypeStr = material.getContentType();
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (contentTypeStr != null && !contentTypeStr.isBlank()) {
            try {
                mediaType = MediaType.parseMediaType(contentTypeStr);
            } catch (Exception ignored) {
            }
        }

        String fileName = material.getFileName() != null ? material.getFileName() : "document_" + id;
        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(fileName)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDisposition(contentDisposition);
        headers.setContentLength(fileData.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createMaterial(@ModelAttribute MaterialUploadDto uploadDto, HttpServletRequest request) {
        List<FieldErrorDto> fieldErrors = new ArrayList<>();
        if (uploadDto == null || uploadDto.getTitle() == null || uploadDto.getTitle().trim().isEmpty()) {
            fieldErrors.add(new FieldErrorDto("title", "Title is required"));
        }

        if (!fieldErrors.isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    "VALIDATION_ERROR",
                    "Invalid material upload request",
                    request.getRequestURI(),
                    fieldErrors
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        MaterialDto created = materialService.createMaterial(uploadDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateMaterial(
            @PathVariable("id") Long id,
            @ModelAttribute MaterialUploadDto uploadDto,
            HttpServletRequest request) {
        List<FieldErrorDto> fieldErrors = new ArrayList<>();
        if (uploadDto == null || uploadDto.getTitle() == null || uploadDto.getTitle().trim().isEmpty()) {
            fieldErrors.add(new FieldErrorDto("title", "Title is required"));
        }

        if (!fieldErrors.isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    "VALIDATION_ERROR",
                    "Invalid material update request",
                    request.getRequestURI(),
                    fieldErrors
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        Optional<MaterialDto> updated = materialService.updateMaterial(id, uploadDto);
        if (updated.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updated.get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable("id") Long id) {
        boolean deleted = materialService.deleteMaterial(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "BAD_REQUEST",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
