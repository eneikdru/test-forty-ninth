package com.eneik.epidemiology.controller;

import com.eneik.epidemiology.model.MaterialDocumentContent;
import com.eneik.epidemiology.model.MaterialSearchResponse;
import com.eneik.epidemiology.service.MaterialSearchService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/materials")
public class MaterialSearchController {

    private final MaterialSearchService materialSearchService;

    public MaterialSearchController(MaterialSearchService materialSearchService) {
        this.materialSearchService = materialSearchService;
    }

    @GetMapping("/search")
    public ResponseEntity<MaterialSearchResponse> searchMaterials(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        if (page < 0 || size < 1) {
            return ResponseEntity.badRequest().build();
        }

        MaterialSearchResponse response = materialSearchService.search(query, category, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable("id") String id) {
        return materialSearchService.getDocumentContent(id)
                .map(doc -> {
                    ByteArrayResource resource = new ByteArrayResource(doc.getContent());
                    MediaType mediaType = MediaType.parseMediaType(
                            doc.getContentType() != null ? doc.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE
                    );

                    return ResponseEntity.ok()
                            .contentType(mediaType)
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                            .contentLength(doc.getContent().length)
                            .body((Resource) resource);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
