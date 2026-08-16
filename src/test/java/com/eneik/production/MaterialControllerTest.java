package com.eneik.production;

import com.eneik.production.models.persistence.MaterialEntity;
import com.eneik.production.repository.MaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MaterialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MaterialRepository materialRepository;

    @BeforeEach
    void setUp() {
        materialRepository.deleteAll();
    }

    @Test
    void testSearchMaterialsEndpoint() throws Exception {
        MaterialEntity m1 = new MaterialEntity("Flu Guidelines", "Seasonal influenza management", "Text", "flu.pdf", "application/pdf", "dummy pdf content".getBytes());
        MaterialEntity m2 = new MaterialEntity("COVID Protocol", "Coronavirus response details", "Text", "covid.pdf", "application/pdf", "dummy covid content".getBytes());

        materialRepository.save(m1);
        materialRepository.save(m2);

        mockMvc.perform(get("/api/materials/search")
                        .param("query", "Flu")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Flu Guidelines")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void testDownloadDocumentEndpoint() throws Exception {
        byte[] contentBytes = "Sample document file binary content".getBytes();
        MaterialEntity material = new MaterialEntity("Sample Doc", "Description", "Content", "test_doc.pdf", "application/pdf", contentBytes);
        MaterialEntity saved = materialRepository.save(material);

        mockMvc.perform(get("/api/materials/{id}/download", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test_doc.pdf\""))
                .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, contentBytes.length))
                .andExpect(content().bytes(contentBytes));
    }

    @Test
    void testDownloadDocumentNotFound() throws Exception {
        mockMvc.perform(get("/api/materials/99999/download"))
                .andExpect(status().isNotFound());
    }
}
