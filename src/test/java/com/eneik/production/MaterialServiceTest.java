package com.eneik.production;

import com.eneik.production.dto.MaterialDto;
import com.eneik.production.models.persistence.MaterialEntity;
import com.eneik.production.repository.MaterialRepository;
import com.eneik.production.service.MaterialService;
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
    private MaterialService materialService;

    @BeforeEach
    void setUp() {
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
    }
}
