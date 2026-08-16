package com.eneik.production.controller;

import com.eneik.production.dto.MaterialDto;
import com.eneik.production.models.persistence.MaterialEntity;
import com.eneik.production.service.MaterialService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
