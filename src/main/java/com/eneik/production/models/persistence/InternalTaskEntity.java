package com.eneik.production.models.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "internal_tasks")
public class InternalTaskEntity {

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "github_pr_number")
    private Integer githubPrNumber;

    @Column(name = "github_pr_state", length = 50)
    private String githubPrState;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public InternalTaskEntity() {
    }

    public InternalTaskEntity(String id, String title, String status, Integer githubPrNumber, String githubPrState, LocalDateTime createdAt, LocalDateTime updatedAt) {
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
