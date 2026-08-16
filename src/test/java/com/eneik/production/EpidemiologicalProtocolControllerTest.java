package com.eneik.production;

import com.eneik.production.dto.EpidemiologicalProtocolRequestDto;
import com.eneik.production.models.persistence.EpidemiologicalProtocolEntity;
import com.eneik.production.repository.EpidemiologicalProtocolRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class EpidemiologicalProtocolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EpidemiologicalProtocolRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSearchAndFilterProtocols() throws Exception {
        // Search by query "COVID"
        mockMvc.perform(get("/api/protocols")
                        .param("query", "COVID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].code", is("EPI-PROTO-001")));

        // Search and filter by category "Enteric"
        mockMvc.perform(get("/api/protocols/search")
                        .param("category", "Enteric"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void testGetProtocolByIdAndCode() throws Exception {
        EpidemiologicalProtocolEntity p = new EpidemiologicalProtocolEntity(
                "EPI-TEST-003",
                "Influenza Rapid Response",
                "Respiratory",
                "v1.0",
                "APPROVED",
                "Flu outbreak response",
                "NIH",
                2023
        );
        EpidemiologicalProtocolEntity saved = repository.save(p);

        try {
            // Get by ID
            mockMvc.perform(get("/api/protocols/{id}", saved.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is("EPI-TEST-003")))
                    .andExpect(jsonPath("$.title", is("Influenza Rapid Response")));

            // Get by Code
            mockMvc.perform(get("/api/protocols/code/{code}", "EPI-TEST-003"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(saved.getId().intValue())));
        } finally {
            repository.deleteById(saved.getId());
        }
    }

    @Test
    void testCreateUpdateAndDeleteProtocol() throws Exception {
        EpidemiologicalProtocolRequestDto createRequest = new EpidemiologicalProtocolRequestDto(
                "EPI-TEST-004",
                "Monkeypox Surveillance Protocol",
                "Zoonotic",
                "v1.0",
                "DRAFT",
                "Monkeypox guidelines",
                "ECDC",
                2024
        );

        // Create
        String responseContent = mockMvc.perform(post("/api/protocols")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is("EPI-TEST-004")))
                .andExpect(jsonPath("$.title", is("Monkeypox Surveillance Protocol")))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(responseContent).get("id").asLong();

        // Update
        EpidemiologicalProtocolRequestDto updateRequest = new EpidemiologicalProtocolRequestDto(
                "EPI-TEST-004",
                "Mpox Surveillance Protocol Updated",
                "Zoonotic",
                "v1.1",
                "APPROVED",
                "Updated guidelines",
                "ECDC",
                2024
        );

        mockMvc.perform(put("/api/protocols/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Mpox Surveillance Protocol Updated")))
                .andExpect(jsonPath("$.status", is("APPROVED")));

        // Delete
        mockMvc.perform(delete("/api/protocols/{id}", id))
                .andExpect(status().isNoContent());

        // Verify missing
        mockMvc.perform(get("/api/protocols/{id}", id))
                .andExpect(status().isNotFound());
    }
}
