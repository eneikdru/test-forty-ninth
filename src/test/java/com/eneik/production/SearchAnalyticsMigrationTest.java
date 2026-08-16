package com.eneik.production;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class SearchAnalyticsMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testTableAndIndicesExist() {
        // Query H2 system tables to check if search_analytics_events exists
        Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'SEARCH_ANALYTICS_EVENTS'",
                Integer.class
        );
        assertEquals(1, tableCount, "Table SEARCH_ANALYTICS_EVENTS must exist after migration");

        // Query H2 system tables for indices on SEARCH_ANALYTICS_EVENTS
        List<Map<String, Object>> indexList = jdbcTemplate.queryForList(
                "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_NAME = 'SEARCH_ANALYTICS_EVENTS'"
        );

        List<String> indexNames = indexList.stream()
                .map(m -> (String) m.get("INDEX_NAME"))
                .map(String::toUpperCase)
                .toList();

        assertTrue(indexNames.contains("IDX_SEARCH_EVENTS_CREATED_AT"), "Index IDX_SEARCH_EVENTS_CREATED_AT must exist");
        assertTrue(indexNames.contains("IDX_SEARCH_EVENTS_QUERY"), "Index IDX_SEARCH_EVENTS_QUERY must exist");
        assertTrue(indexNames.contains("IDX_SEARCH_EVENTS_USER_ID"), "Index IDX_SEARCH_EVENTS_USER_ID must exist");
    }
}
