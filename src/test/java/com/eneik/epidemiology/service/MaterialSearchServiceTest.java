package com.eneik.epidemiology.service;

import com.eneik.epidemiology.model.EpidemiologicalMaterial;
import com.eneik.epidemiology.model.MaterialDocumentContent;
import com.eneik.epidemiology.model.MaterialSearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MaterialSearchServiceTest {

    private MaterialSearchService service;

    @BeforeEach
    void setUp() {
        service = new MaterialSearchService();
    }

    @Test
    void search_returnsAllMaterials_whenQueryAndCategoryAreNull() {
        MaterialSearchResponse response = service.search(null, null, 0, 10);

        assertNotNull(response);
        assertEquals(4, response.getItems().size());
        assertEquals(0, response.getPage().getPage());
        assertEquals(10, response.getPage().getSize());
        assertEquals(4, response.getPage().getTotalElements());
        assertEquals(1, response.getPage().getTotalPages());
    }

    @Test
    void search_filtersByQueryCaseInsensitive() {
        MaterialSearchResponse response = service.search("cholera", null, 0, 10);

        assertEquals(1, response.getItems().size());
        EpidemiologicalMaterial item = response.getItems().get(0);
        assertEquals("mat-101", item.getId());
        assertTrue(item.getTitle().toLowerCase().contains("cholera"));
    }

    @Test
    void search_filtersByCategory() {
        MaterialSearchResponse response = service.search(null, "protocol", 0, 10);

        assertEquals(1, response.getItems().size());
        assertEquals("protocol", response.getItems().get(0).getCategory());
    }

    @Test
    void search_appliesPaginationCorrectly() {
        MaterialSearchResponse page0 = service.search(null, null, 0, 2);
        assertEquals(2, page0.getItems().size());
        assertEquals("mat-101", page0.getItems().get(0).getId());
        assertEquals("mat-102", page0.getItems().get(1).getId());
        assertEquals(4, page0.getPage().getTotalElements());
        assertEquals(2, page0.getPage().getTotalPages());

        MaterialSearchResponse page1 = service.search(null, null, 1, 2);
        assertEquals(2, page1.getItems().size());
        assertEquals("mat-103", page1.getItems().get(0).getId());
        assertEquals("mat-104", page1.getItems().get(1).getId());
    }

    @Test
    void search_returnsEmptyList_whenQueryMatchesNothing() {
        MaterialSearchResponse response = service.search("nonexistentquery", null, 0, 10);

        assertTrue(response.getItems().isEmpty());
        assertEquals(0, response.getPage().getTotalElements());
    }

    @Test
    void getDocumentContent_returnsDocument_whenIdExists() {
        Optional<MaterialDocumentContent> content = service.getDocumentContent("mat-101");

        assertTrue(content.isPresent());
        assertEquals("cholera_protocol_v1.pdf", content.get().getFileName());
        assertEquals("application/pdf", content.get().getContentType());
        assertNotNull(content.get().getContent());
        assertTrue(content.get().getContent().length > 0);
    }

    @Test
    void getDocumentContent_returnsEmpty_whenIdDoesNotExist() {
        Optional<MaterialDocumentContent> content = service.getDocumentContent("nonexistent-id");

        assertTrue(content.isEmpty());
    }
}
