package com.eneik.production.dto;

public class SearchEventRequestDTO {

    private String query;
    private String userId;
    private String filters;
    private Integer resultCount;
    private Long executionTimeMs;

    public SearchEventRequestDTO() {
    }

    public SearchEventRequestDTO(String query, String userId, String filters, Integer resultCount, Long executionTimeMs) {
        this.query = query;
        this.userId = userId;
        this.filters = filters;
        this.resultCount = resultCount;
        this.executionTimeMs = executionTimeMs;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFilters() {
        return filters;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }

    public Integer getResultCount() {
        return resultCount;
    }

    public void setResultCount(Integer resultCount) {
        this.resultCount = resultCount;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
}
