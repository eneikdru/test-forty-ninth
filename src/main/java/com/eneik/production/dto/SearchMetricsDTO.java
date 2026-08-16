package com.eneik.production.dto;

public class SearchMetricsDTO {

    private long totalSearches;
    private double averageExecutionTimeMs;
    private long uniqueUsersCount;
    private double zeroResultRate;

    public SearchMetricsDTO() {
    }

    public SearchMetricsDTO(long totalSearches, double averageExecutionTimeMs, long uniqueUsersCount, double zeroResultRate) {
        this.totalSearches = totalSearches;
        this.averageExecutionTimeMs = averageExecutionTimeMs;
        this.uniqueUsersCount = uniqueUsersCount;
        this.zeroResultRate = zeroResultRate;
    }

    public long getTotalSearches() {
        return totalSearches;
    }

    public void setTotalSearches(long totalSearches) {
        this.totalSearches = totalSearches;
    }

    public double getAverageExecutionTimeMs() {
        return averageExecutionTimeMs;
    }

    public void setAverageExecutionTimeMs(double averageExecutionTimeMs) {
        this.averageExecutionTimeMs = averageExecutionTimeMs;
    }

    public long getUniqueUsersCount() {
        return uniqueUsersCount;
    }

    public void setUniqueUsersCount(long uniqueUsersCount) {
        this.uniqueUsersCount = uniqueUsersCount;
    }

    public double getZeroResultRate() {
        return zeroResultRate;
    }

    public void setZeroResultRate(double zeroResultRate) {
        this.zeroResultRate = zeroResultRate;
    }
}
