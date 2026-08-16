package com.eneik.production.search;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchQueryBuilderTest {

    @Test
    void testConstructsSimpleTextQuery() {
        SearchQueryBuilder builder = new SearchQueryBuilder();
        SearchQueryBuilder.SearchQuery searchQuery = builder
                .withQuery("influenza")
                .build();

        assertEquals("influenza", searchQuery.getQuery());
        assertNull(searchQuery.getCategory());
        assertTrue(searchQuery.getTags().isEmpty());
        assertTrue(searchQuery.hasTextQuery());
        assertFalse(searchQuery.hasFilters());
        assertEquals("(LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(m.description) LIKE LOWER(CONCAT('%', :query, '%')))", searchQuery.buildJpqlWhereClause());
    }

    @Test
    void testConstructsComplexQueryWithFilters() {
        SearchQueryBuilder builder = new SearchQueryBuilder();
        SearchQueryBuilder.SearchQuery searchQuery = builder
                .withQuery("outbreak")
                .withCategory("protocol")
                .withContentType("application/pdf")
                .withTag("influenza")
                .withTag("surveillance")
                .withMetadataFilter("author", "Epidemiology Group")
                .build();

        assertEquals("outbreak", searchQuery.getQuery());
        assertEquals("protocol", searchQuery.getCategory());
        assertEquals("application/pdf", searchQuery.getContentType());
        assertEquals(List.of("influenza", "surveillance"), searchQuery.getTags());
        assertEquals("Epidemiology Group", searchQuery.getMetadataFilters().get("author"));
        assertTrue(searchQuery.hasTextQuery());
        assertTrue(searchQuery.hasFilters());

        String whereClause = searchQuery.buildJpqlWhereClause();
        assertTrue(whereClause.contains(":query"));
        assertTrue(whereClause.contains("m.category = :category"));
        assertTrue(whereClause.contains("m.contentType = :contentType"));
    }

    @Test
    void testHandlesNullAndEmptyFilterInputs() {
        SearchQueryBuilder builder = new SearchQueryBuilder();
        SearchQueryBuilder.SearchQuery searchQuery = builder
                .withQuery("  COVID-19  ")
                .withCategory(null)
                .withTag("")
                .withTag("   ")
                .withMetadataFilter("   ", "val")
                .withMetadataFilter("key", null)
                .build();

        assertEquals("COVID-19", searchQuery.getQuery());
        assertNull(searchQuery.getCategory());
        assertTrue(searchQuery.getTags().isEmpty());
        assertTrue(searchQuery.getMetadataFilters().isEmpty());
    }
}
