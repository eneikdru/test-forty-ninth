package com.eneik.production.dto;

import java.time.OffsetDateTime;

public class InternalTaskDto {
    private String id;
    private String title;
    private String status;
    private Integer githubPrNumber;
    private String githubPrState;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public InternalTaskDto() {
    }

    public InternalTaskDto(String id, String title, String status, Integer githubPrNumber, String githubPrState, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.githubPrNumber = githubPrNumber;
        this.githubPrState = githubPrState;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getGithubPrNumber() {
        return githubPrNumber;
    }

    public void setGithubPrNumber(Integer githubPrNumber) {
        this.githubPrNumber = githubPrNumber;
    }

    public String getGithubPrState() {
        return githubPrState;
    }

    public void setGithubPrState(String githubPrState) {
        this.githubPrState = githubPrState;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
