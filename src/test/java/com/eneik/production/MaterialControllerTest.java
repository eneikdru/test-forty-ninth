package com.eneik.production;

import com.eneik.production.models.persistence.MaterialEntity;
import com.eneik.production.repository.MaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
    @WithMockUser(username="user",roles={"ADMIN"})
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
    @WithMockUser(username="user",roles={"ADMIN"})
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
    @WithMockUser(username="user",roles={"ADMIN"})
    void testDownloadDocumentNotFound() throws Exception {
        mockMvc.perform(get("/api/materials/99999/download"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username="user",roles={"ADMIN"})
    void testCreateMaterialSuccess() throws Exception {
        byte[] fileBytes = "PDF report content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "report.pdf", "application/pdf", fileBytes);

        mockMvc.perform(multipart("/api/materials")
                        .file(file)
                        .param("title", "Epidemiological Report 2026")
                        .param("description", "Annual influenza surveillance data")
                        .param("content", "Full text of epidemiological findings...")
                        .param("category", "protocol")
                        .param("tags", "influenza", "outbreak"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("Epidemiological Report 2026")))
                .andExpect(jsonPath("$.description", is("Annual influenza surveillance data")))
                .andExpect(jsonPath("$.category", is("protocol")))
                .andExpect(jsonPath("$.tags", containsInAnyOrder("influenza", "outbreak")))
                .andExpect(jsonPath("$.fileName", is("report.pdf")))
                .andExpect(jsonPath("$.contentType", is("application/pdf")));

        assertEquals(1, materialRepository.count());
        MaterialEntity saved = materialRepository.findAll().get(0);
        assertEquals("Epidemiological Report 2026", saved.getTitle());
        assertEquals("protocol", saved.getCategory());
        assertEquals(2, saved.getTags().size());
        assertTrue(saved.getTags().contains("influenza"));
        assertTrue(saved.getTags().contains("outbreak"));
        assertEquals("report.pdf", saved.getFileName());
        assertEquals("application/pdf", saved.getContentType());
        assertArrayEquals(fileBytes, saved.getFileData());
    }

    @Test
    @WithMockUser(username="user",roles={"ADMIN"})
    void testCreateMaterialValidationErrorMissingTitle() throws Exception {
        mockMvc.perform(multipart("/api/materials")
                        .param("description", "Missing title description"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.details", hasSize(1)))
                .andExpect(jsonPath("$.details[0].field", is("title")))
                .andExpect(jsonPath("$.details[0].message", is("Title is required")));

        assertEquals(0, materialRepository.count());
    }

    @Test
    void testCreateMaterialUnauthenticatedReturns401() throws Exception {
        byte[] fileBytes = "PDF report content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "report.pdf", "application/pdf", fileBytes);

        mockMvc.perform(multipart("/api/materials")
                        .file(file)
                        .param("title", "Epidemiological Report 2026"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="regularUser", roles={"USER"})
    void testCreateMaterialForbiddenReturns403() throws Exception {
        byte[] fileBytes = "PDF report content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "report.pdf", "application/pdf", fileBytes);

        mockMvc.perform(multipart("/api/materials")
                        .file(file)
                        .param("title", "Epidemiological Report 2026"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateMaterialUnauthenticatedReturns401() throws Exception {
        mockMvc.perform(multipart("/api/materials/1")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .param("title", "Updated Title"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="regularUser", roles={"USER"})
    void testUpdateMaterialForbiddenReturns403() throws Exception {
        mockMvc.perform(multipart("/api/materials/1")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .param("title", "Updated Title"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteMaterialUnauthenticatedReturns401() throws Exception {
        mockMvc.perform(delete("/api/materials/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="regularUser", roles={"USER"})
    void testDeleteMaterialForbiddenReturns403() throws Exception {
        mockMvc.perform(delete("/api/materials/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username="user",roles={"ADMIN"})
    void testUpdateMaterialSuccess() throws Exception {
        MaterialEntity material = new MaterialEntity("Original Title", "Original Desc", "Original Content", "report", List.of("oldtag"), "orig.pdf", "application/pdf", "orig bytes".getBytes());
        MaterialEntity saved = materialRepository.save(material);

        byte[] updatedFileBytes = "Updated PDF report content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "updated_report.pdf", "application/pdf", updatedFileBytes);

        mockMvc.perform(multipart("/api/materials/{id}", saved.getId())
                        .file(file)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .param("title", "Updated Title")
                        .param("description", "Updated Desc")
                        .param("content", "Updated Content")
                        .param("category", "protocol")
                        .param("tags", "newtag1", "newtag2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Updated Title")))
                .andExpect(jsonPath("$.description", is("Updated Desc")))
                .andExpect(jsonPath("$.content", is("Updated Content")))
                .andExpect(jsonPath("$.category", is("protocol")))
                .andExpect(jsonPath("$.tags", containsInAnyOrder("newtag1", "newtag2")))
                .andExpect(jsonPath("$.fileName", is("updated_report.pdf")))
                .andExpect(jsonPath("$.contentType", is("application/pdf")));

        MaterialEntity updated = materialRepository.findById(saved.getId()).orElseThrow();
        assertEquals("Updated Title", updated.getTitle());
        assertEquals("Updated Desc", updated.getDescription());
        assertEquals("Updated Content", updated.getContent());
        assertEquals("protocol", updated.getCategory());
        assertEquals(2, updated.getTags().size());
        assertTrue(updated.getTags().contains("newtag1"));
        assertTrue(updated.getTags().contains("newtag2"));
        assertEquals("updated_report.pdf", updated.getFileName());
        assertArrayEquals(updatedFileBytes, updated.getFileData());
    }

    @Test
    @WithMockUser(username="user",roles={"ADMIN"})
    void testUpdateMaterialValidationErrorMissingTitle() throws Exception {
        MaterialEntity material = new MaterialEntity("Original Title", "Original Desc", "Original Content", null, null, null);
        MaterialEntity saved = materialRepository.save(material);

        mockMvc.perform(multipart("/api/materials/{id}", saved.getId())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .param("description", "Updated Desc Without Title"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.details[0].field", is("title")));
    }

    @Test
    @WithMockUser(username="user",roles={"ADMIN"})
    void testUpdateMaterialNotFound() throws Exception {
        mockMvc.perform(multipart("/api/materials/99999")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .param("title", "Updated Title"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username="user",roles={"ADMIN"})
    void testDeleteMaterialSuccess() throws Exception {
        MaterialEntity material = new MaterialEntity("Title to delete", "Desc", "Content", null, null, null);
        MaterialEntity saved = materialRepository.save(material);

        mockMvc.perform(delete("/api/materials/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        assertFalse(materialRepository.existsById(saved.getId()));
    }

    @Test
    @WithMockUser(username="user",roles={"ADMIN"})
    void testDeleteMaterialNotFound() throws Exception {
        mockMvc.perform(delete("/api/materials/99999"))
                .andExpect(status().isNotFound());
    }
}
