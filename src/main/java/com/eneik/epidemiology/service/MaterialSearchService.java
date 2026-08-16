package com.eneik.epidemiology.service;

import com.eneik.epidemiology.model.EpidemiologicalMaterial;
import com.eneik.epidemiology.model.MaterialDocumentContent;
import com.eneik.epidemiology.model.MaterialSearchResponse;
import com.eneik.epidemiology.model.PageMetadata;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class MaterialSearchService {

    private final List<EpidemiologicalMaterial> repository = new ArrayList<>();
    private final Map<String, MaterialDocumentContent> documentStore = new ConcurrentHashMap<>();

    public MaterialSearchService() {
        initSampleData();
    }

    private void initSampleData() {
        EpidemiologicalMaterial m1 = new EpidemiologicalMaterial(
                "mat-101",
                "Cholera Outbreak Control Protocol",
                "Standard operating procedure for epidemiological field investigation during cholera outbreaks.",
                "protocol",
                "Dr. Elena Rostova",
                LocalDate.of(2026, 1, 15),
                "cholera_protocol_v1.pdf",
                204800L
        );

        EpidemiologicalMaterial m2 = new EpidemiologicalMaterial(
                "mat-102",
                "Influenza Surveillance Data Analysis",
                "Statistical report on seasonal influenza surveillance and vaccine efficacy data.",
                "research",
                "Dr. Alexander Petrov",
                LocalDate.of(2026, 2, 10),
                "influenza_surveillance_2026.pdf",
                512000L
        );

        EpidemiologicalMaterial m3 = new EpidemiologicalMaterial(
                "mat-103",
                "Ebola Outbreak Response Field Manual",
                "Operational guidelines for containment, contact tracing, and personal protection in Ebola outbreak zones.",
                "guideline",
                "Dr. Maria Sidorova",
                LocalDate.of(2025, 11, 20),
                "ebola_field_manual.pdf",
                1048576L
        );

        EpidemiologicalMaterial m4 = new EpidemiologicalMaterial(
                "mat-104",
                "COVID-19 Genomic Epidemiology Summary",
                "Overview of viral lineage tracking, variant surveillance, and genomic sequencing datasets.",
                "data",
                "Dr. Ivan Volkov",
                LocalDate.of(2026, 3, 1),
                "covid19_genomic_summary.pdf",
                307200L
        );

        repository.add(m1);
        repository.add(m2);
        repository.add(m3);
        repository.add(m4);

        documentStore.put("mat-101", new MaterialDocumentContent("mat-101", "cholera_protocol_v1.pdf", "application/pdf",
                "PDF-1.7 Content for Cholera Outbreak Control Protocol".getBytes(StandardCharsets.UTF_8)));
        documentStore.put("mat-102", new MaterialDocumentContent("mat-102", "influenza_surveillance_2026.pdf", "application/pdf",
                "PDF-1.7 Content for Influenza Surveillance Data Analysis".getBytes(StandardCharsets.UTF_8)));
        documentStore.put("mat-103", new MaterialDocumentContent("mat-103", "ebola_field_manual.pdf", "application/pdf",
                "PDF-1.7 Content for Ebola Outbreak Response Field Manual".getBytes(StandardCharsets.UTF_8)));
        documentStore.put("mat-104", new MaterialDocumentContent("mat-104", "covid19_genomic_summary.pdf", "application/pdf",
                "PDF-1.7 Content for COVID-19 Genomic Epidemiology Summary".getBytes(StandardCharsets.UTF_8)));
    }

    public MaterialSearchResponse search(String query, String category, int page, int size) {
        List<EpidemiologicalMaterial> filtered = repository.stream()
                .filter(m -> matchesQuery(m, query))
                .filter(m -> matchesCategory(m, category))
                .collect(Collectors.toList());

        int totalElements = filtered.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        if (totalPages == 0) {
            totalPages = 1;
        }

        int fromIndex = page * size;
        List<EpidemiologicalMaterial> pagedItems;
        if (fromIndex >= totalElements) {
            pagedItems = List.of();
        } else {
            int toIndex = Math.min(fromIndex + size, totalElements);
            pagedItems = filtered.subList(fromIndex, toIndex);
        }

        PageMetadata pageMetadata = new PageMetadata(page, size, totalElements, totalPages);
        return new MaterialSearchResponse(pagedItems, pageMetadata);
    }

    public Optional<MaterialDocumentContent> getDocumentContent(String id) {
        return Optional.ofNullable(documentStore.get(id));
    }

    private boolean matchesQuery(EpidemiologicalMaterial material, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String q = query.toLowerCase();
        return (material.getTitle() != null && material.getTitle().toLowerCase().contains(q))
                || (material.getDescription() != null && material.getDescription().toLowerCase().contains(q))
                || (material.getAuthor() != null && material.getAuthor().toLowerCase().contains(q));
    }

    private boolean matchesCategory(EpidemiologicalMaterial material, String category) {
        if (category == null || category.isBlank()) {
            return true;
        }
        return material.getCategory() != null && material.getCategory().equalsIgnoreCase(category.trim());
    }
}
