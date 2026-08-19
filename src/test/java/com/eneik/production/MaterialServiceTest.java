package com.eneik.production;

import com.eneik.production.dto.MaterialDto;
import com.eneik.production.dto.MaterialUploadDto;
import com.eneik.production.dto.SearchMetricsDTO;
import com.eneik.production.models.persistence.MaterialEntity;
import java.util.List;
import com.eneik.production.repository.MaterialRepository;
import com.eneik.production.repository.SearchAnalyticsEventRepository;
import com.eneik.production.service.MaterialService;
import com.eneik.production.service.SearchAnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MaterialServiceTest {

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private SearchAnalyticsEventRepository searchAnalyticsEventRepository;

    @Autowired
    private SearchAnalyticsService searchAnalyticsService;

    @Autowired
    private MaterialService materialService;

    @BeforeEach
    void setUp() {
        searchAnalyticsEventRepository.deleteAll();
        materialRepository.deleteAll();
    }

    @Test
    void testSearchMaterialsPaginationAndFiltering() {
        MaterialEntity m1 = new MaterialEntity("Epidemiology Protocol A", "General guidance for outbreak", "Content A", "protocol_a.pdf", "application/pdf", "data1".getBytes());
        MaterialEntity m2 = new MaterialEntity("Epidemiology Protocol B", "Specific guidance for virus", "Content B", "protocol_b.pdf", "application/pdf", "data2".getBytes());
        MaterialEntity m3 = new MaterialEntity("Data Report 2026", "Annual report", "Content C", "report.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "data3".getBytes());

        materialService.saveMaterial(m1);
        materialService.saveMaterial(m2);
        materialService.saveMaterial(m3);

        Page<MaterialDto> page1 = materialService.searchMaterials("Protocol", PageRequest.of(0, 1));
        assertEquals(2, page1.getTotalElements());
        assertEquals(2, page1.getTotalPages());
        assertEquals(1, page1.getContent().size());
        assertTrue(page1.getContent().get(0).getTitle().contains("Protocol"));

        Page<MaterialDto> page2 = materialService.searchMaterials("Protocol", PageRequest.of(1, 1));
        assertEquals(1, page2.getContent().size());
        assertTrue(page2.getContent().get(0).getTitle().contains("Protocol"));

        Page<MaterialDto> pageAll = materialService.searchMaterials("", PageRequest.of(0, 10));
        assertEquals(3, pageAll.getTotalElements());

        // Perform zero result search to verify telemetry records empty query
        Page<MaterialDto> pageEmpty = materialService.searchMaterials("nonexistent_term", PageRequest.of(0, 10));
        assertEquals(0, pageEmpty.getTotalElements());

        SearchMetricsDTO metrics = searchAnalyticsService.getAggregateMetrics();
        assertEquals(4, metrics.getTotalSearches());
        assertEquals(0.25, metrics.getZeroResultRate(), 0.001);
    }

    @Test
    void testMaterialCategoryAndTagsPersistence() {
        MaterialUploadDto uploadDto = new MaterialUploadDto(
                "Cholera Surveillance Protocol",
                "Guidelines for monitoring waterborne diseases",
                "Protocol details for cholera outbreak management...",
                "Epidemiology",
                List.of("outbreak", "cholera", "surveillance"),
                null
        );

        MaterialDto created = materialService.createMaterial(uploadDto);
        assertNotNull(created.getId());
        assertEquals("Epidemiology", created.getCategory());
        assertEquals(3, created.getTags().size());
        assertTrue(created.getTags().contains("outbreak"));
        assertTrue(created.getTags().contains("cholera"));
        assertTrue(created.getTags().contains("surveillance"));

        MaterialEntity entity = materialService.getMaterialEntity(created.getId()).orElseThrow();
        assertEquals("Epidemiology", entity.getCategory());
        assertEquals(3, entity.getTags().size());
        assertTrue(entity.getTags().contains("outbreak"));
        assertTrue(entity.getTags().contains("cholera"));
        assertTrue(entity.getTags().contains("surveillance"));
    }
}
