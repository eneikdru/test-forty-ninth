package com.eneik.production;

import com.eneik.production.models.persistence.MaterialEntity;
import com.eneik.production.repository.MaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SearchApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MaterialRepository materialRepository;

    @BeforeEach
    void setUp() {
        materialRepository.deleteAll();
    }

    @Test
    void testSearchApiAccuratelyFiltersDatabaseRecordsByMetadata() throws Exception {
        MaterialEntity m1 = new MaterialEntity("Influenza Surveillance Protocol 2026", "Protocol for field viral tracking", "Detailed influenza tracking procedure", "flu_protocol.pdf", "application/pdf", "pdf-data".getBytes());
        MaterialEntity m2 = new MaterialEntity("SARS Outbreak Guidelines", "Clinical guidelines for emergency respiratory containment", "Emergency response steps for SARS", "sars_guidelines.pdf", "application/pdf", "pdf-data-2".getBytes());
        MaterialEntity m3 = new MaterialEntity("Q3 Epidemiological Pathogen Dataset", "Raw statistical records for viral pathogens", "CSV dataset with weekly counts", "q3_dataset.csv", "text/csv", "csv-data".getBytes());

        materialRepository.save(m1);
        materialRepository.save(m2);
        materialRepository.save(m3);

        // Filter by query "Influenza"
        mockMvc.perform(get("/api/materials/search")
                        .param("query", "Influenza")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Influenza Surveillance Protocol 2026")))
                .andExpect(jsonPath("$.content[0].contentType", is("application/pdf")))
                .andExpect(jsonPath("$.totalElements", is(1)));

        // Filter by query "respiratory" metadata in description
        mockMvc.perform(get("/api/materials/search")
                        .param("query", "respiratory")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("SARS Outbreak Guidelines")))
                .andExpect(jsonPath("$.totalElements", is(1)));

        // Query returning all records when empty
        mockMvc.perform(get("/api/materials/search")
                        .param("query", "")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }
}
