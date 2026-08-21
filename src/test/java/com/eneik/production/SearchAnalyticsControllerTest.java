package com.eneik.production;

import com.eneik.production.dto.SearchEventRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.test.context.support.WithMockUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SearchAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username="user",roles={"ADMIN"})
    public void testRecordSearchEventAndGetMetrics() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/search/events/reset")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        SearchEventRequestDTO event1 = new SearchEventRequestDTO(
                "ebola protocol", "user_100", "{\"category\":\"protocol\"}", 10, 150L
        );
        SearchEventRequestDTO event2 = new SearchEventRequestDTO(
                "rare disease data", "user_101", "{\"category\":\"data\"}", 0, 50L
        );
        SearchEventRequestDTO event3 = new SearchEventRequestDTO(
                "ebola guidelines", "user_100", "{\"category\":\"guidelines\"}", 5, 250L
        );

        mockMvc.perform(post("/api/v1/analytics/search/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.query", is("ebola protocol")))
                .andExpect(jsonPath("$.userId", is("user_100")))
                .andExpect(jsonPath("$.resultCount", is(10)))
                .andExpect(jsonPath("$.executionTimeMs", is(150)));

        mockMvc.perform(post("/api/v1/analytics/search/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/analytics/search/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event3)))
                .andExpect(status().isCreated());

        // Fetch aggregate metrics and type-safely assert values
        MvcResult result = mockMvc.perform(get("/api/v1/analytics/search/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSearches", is(3)))
                .andExpect(jsonPath("$.uniqueUsersCount", is(2)))
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        Map<?, ?> responseMap = objectMapper.readValue(jsonResponse, Map.class);

        double avgExecTime = ((Number) responseMap.get("averageExecutionTimeMs")).doubleValue();
        double zeroResultRate = ((Number) responseMap.get("zeroResultRate")).doubleValue();

        assertEquals(150.0, avgExecTime, 0.001);
        assertEquals(0.3333, zeroResultRate, 0.001);
    }
}
