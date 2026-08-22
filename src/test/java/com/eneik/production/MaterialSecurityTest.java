package com.eneik.production;

import com.eneik.production.security.SecurityConfig;
import com.eneik.production.controller.MaterialController;
import com.eneik.production.service.MaterialService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MaterialController.class)
@Import(SecurityConfig.class)
public class MaterialSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MaterialService materialService;

    @Test
    void testPostUnauthenticatedReturns401() throws Exception {
        mockMvc.perform(post("/api/materials")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testPostForbiddenReturns403() throws Exception {
        mockMvc.perform(post("/api/materials")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void testPutUnauthenticatedReturns401() throws Exception {
        mockMvc.perform(put("/api/materials/1")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testPutForbiddenReturns403() throws Exception {
        mockMvc.perform(put("/api/materials/1")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteUnauthenticatedReturns401() throws Exception {
        mockMvc.perform(delete("/api/materials/1").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testDeleteForbiddenReturns403() throws Exception {
        mockMvc.perform(delete("/api/materials/1").with(csrf()))
                .andExpect(status().isForbidden());
    }
}
