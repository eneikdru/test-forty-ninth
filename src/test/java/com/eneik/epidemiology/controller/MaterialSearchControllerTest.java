package com.eneik.epidemiology.controller;

import com.eneik.epidemiology.model.EpidemiologicalMaterial;
import com.eneik.epidemiology.model.MaterialDocumentContent;
import com.eneik.epidemiology.model.MaterialSearchResponse;
import com.eneik.epidemiology.model.PageMetadata;
import com.eneik.epidemiology.service.MaterialSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MaterialSearchController.class)
class MaterialSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MaterialSearchService materialSearchService;

    @Test
    void searchMaterials_returnsPaginatedList() throws Exception {
        EpidemiologicalMaterial mat = new EpidemiologicalMaterial(
                "mat-101",
                "Cholera Outbreak Control Protocol",
                "Protocol for cholera investigation.",
                "protocol",
                "Dr. Elena Rostova",
                LocalDate.of(2026, 1, 15),
                "cholera_protocol_v1.pdf",
                204800L
        );
        PageMetadata metadata = new PageMetadata(0, 10, 1, 1);
        MaterialSearchResponse response = new MaterialSearchResponse(List.of(mat), metadata);

        when(materialSearchService.search(eq("cholera"), eq("protocol"), eq(0), eq(10)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/materials/search")
                        .param("query", "cholera")
                        .param("category", "protocol")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value("mat-101"))
                .andExpect(jsonPath("$.items[0].title").value("Cholera Outbreak Control Protocol"))
                .andExpect(jsonPath("$.items[0].category").value("protocol"))
                .andExpect(jsonPath("$.page.page").value(0))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.page.totalPages").value(1));
    }

    @Test
    void searchMaterials_returnsBadRequest_whenPageNumberIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/materials/search")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void downloadDocument_returnsFileStreamWithHeaders_whenValidRequest() throws Exception {
        byte[] content = "Sample PDF content".getBytes(StandardCharsets.UTF_8);
        MaterialDocumentContent docContent = new MaterialDocumentContent(
                "mat-101",
                "cholera_protocol_v1.pdf",
                "application/pdf",
                content
        );

        when(materialSearchService.getDocumentContent("mat-101"))
                .thenReturn(Optional.of(docContent));

        mockMvc.perform(get("/api/v1/materials/mat-101/download"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cholera_protocol_v1.pdf\""))
                .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, content.length))
                .andExpect(content().bytes(content));
    }

    @Test
    void downloadDocument_returnsNotFound_whenDocumentDoesNotExist() throws Exception {
        when(materialSearchService.getDocumentContent("unknown-id"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/materials/unknown-id/download"))
                .andExpect(status().isNotFound());
    }
}
