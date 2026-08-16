package com.eneik.production.dto;

import java.time.LocalDateTime;

public class InternalTaskDto {
    private String id;
    private String title;
    private String status;
    private Integer githubPrNumber;
    private String githubPrState;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public InternalTaskDto() {
    }

    public InternalTaskDto(String id, String title, String status, Integer githubPrNumber, String githubPrState, LocalDateTime createdAt, LocalDateTime updatedAt) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
