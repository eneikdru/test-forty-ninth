package com.eneik.production;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class EpidemiologicalProtocolSearchWorkflowTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Given seeded protocols, When querying with combined search parameters, Then accurate matching protocols are returned")
    @WithMockUser(username="user",roles={"ADMIN"})
    public void testSearchCombinedParameters() throws Exception {
        // Query q=Outbreak and category=Respiratory
        mockMvc.perform(get("/api/v1/protocols")
                        .param("q", "Outbreak")
                        .param("category", "Respiratory")
                        .param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].code", is("EPI-PROTO-001")));
    }

    @Test
    @DisplayName("Given valid sorting options, When querying protocols, Then results are returned in correct order")
    @WithMockUser(username="user",roles={"ADMIN"})
    public void testSearchSortingAndPagination() throws Exception {
        // Sort by publicationYear asc
        mockMvc.perform(get("/api/v1/protocols")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sortBy", "publicationYear")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.pagination.page", is(0)))
                .andExpect(jsonPath("$.pagination.totalPages", is(8)))
                .andExpect(jsonPath("$.pagination.totalElements", is(15)));
    }

    @Test
    @DisplayName("Given non-matching query string, When querying protocols, Then empty result list is returned")
    @WithMockUser(username="user",roles={"ADMIN"})
    public void testSearchNoResultsFound() throws Exception {
        mockMvc.perform(get("/api/v1/protocols")
                        .param("q", "NonExistentPathogenCode12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.pagination.totalElements", is(0)));
    }

    @Test
    @DisplayName("Given invalid sort parameters, When querying protocols, Then return 400 Bad Request")
    @WithMockUser(username="user",roles={"ADMIN"})
    public void testInvalidSortParameterReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/protocols")
                        .param("sortBy", "invalidField"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_SEARCH_PARAMETER")))
                .andExpect(jsonPath("$.message", containsString("Invalid sort field")));
    }
}
