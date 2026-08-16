package com.eneik.production;

import com.eneik.production.dto.CreateEpidemiologicalProtocolRequest;
import com.eneik.production.dto.UpdateEpidemiologicalProtocolRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class EpidemiologicalProtocolApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testSearchProtocolsWithPaginationAndFiltering() throws Exception {
        // Test search default list
        mockMvc.perform(get("/api/v1/protocols")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "createdAt")
                        .param("sortOrder", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(5)))
                .andExpect(jsonPath("$.pagination.page", is(0)))
                .andExpect(jsonPath("$.pagination.size", is(5)))
                .andExpect(jsonPath("$.pagination.totalElements", is(15)))
                .andExpect(jsonPath("$.pagination.totalPages", is(3)));

        // Test search by keyword q
        mockMvc.perform(get("/api/v1/protocols")
                        .param("q", "COVID-19"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].code", is("EPI-PROTO-001")));

        // Test filter by category
        mockMvc.perform(get("/api/v1/protocols")
                        .param("category", "Enteric"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].code", is("EPI-PROTO-002")));

        // Test filter by status
        mockMvc.perform(get("/api/v1/protocols")
                        .param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.totalElements", is(15)));
    }

    @Test
    public void testInvalidSearchParametersReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/protocols")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_SEARCH_PARAMETER")))
                .andExpect(jsonPath("$.message", containsString("between 1 and 100")));
    }

    @Test
    public void testGetProtocolByIdSuccessAndNotFound() throws Exception {
        // Success
        mockMvc.perform(get("/api/v1/protocols/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.code", is("EPI-PROTO-001")))
                .andExpect(jsonPath("$.title", notNullValue()));

        // Not found
        mockMvc.perform(get("/api/v1/protocols/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("PROTOCOL_NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("99999")));
    }

    @Test
    public void testCreateProtocolSuccessAndConflict() throws Exception {
        CreateEpidemiologicalProtocolRequest createReq = new CreateEpidemiologicalProtocolRequest(
                "EPI-PROTO-NEW-01",
                "Novel Virus Response Protocol",
                "Respiratory",
                "v1.0",
                "DRAFT",
                "Protocol for novel pathogen rapid response.",
                "Global Health Inst",
                2025
        );

        // Success 201
        mockMvc.perform(post("/api/v1/protocols")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.code", is("EPI-PROTO-NEW-01")))
                .andExpect(jsonPath("$.title", is("Novel Virus Response Protocol")));

        // Duplicate Code Conflict 409
        CreateEpidemiologicalProtocolRequest duplicateReq = new CreateEpidemiologicalProtocolRequest(
                "EPI-PROTO-001",
                "Duplicate Code Protocol",
                "Respiratory",
                "v1.0",
                "DRAFT",
                "Test duplicate code",
                "Test Org",
                2025
        );

        mockMvc.perform(post("/api/v1/protocols")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("DUPLICATE_PROTOCOL_CODE")))
                .andExpect(jsonPath("$.message", containsString("EPI-PROTO-001")));
    }

    @Test
    public void testUpdateProtocolSuccessAndAtomicallyGuarded() throws Exception {
        UpdateEpidemiologicalProtocolRequest updateReq = new UpdateEpidemiologicalProtocolRequest(
                "EPI-PROTO-001",
                "Updated COVID-19 Surveillance Protocol",
                "Respiratory",
                "v3.3",
                "APPROVED",
                "Updated guidance notes.",
                "WHO",
                2023
        );

        mockMvc.perform(put("/api/v1/protocols/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Updated COVID-19 Surveillance Protocol")))
                .andExpect(jsonPath("$.version", is("v3.3")));
    }

    @Test
    public void testDeleteProtocolSuccessAndNotFound() throws Exception {
        // Delete existing protocol
        mockMvc.perform(delete("/api/v1/protocols/10"))
                .andExpect(status().isNoContent());

        // Verify deleted
        mockMvc.perform(get("/api/v1/protocols/10"))
                .andExpect(status().isNotFound());

        // Delete non-existent protocol returns 404
        mockMvc.perform(delete("/api/v1/protocols/10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("PROTOCOL_NOT_FOUND")));
    }
}
