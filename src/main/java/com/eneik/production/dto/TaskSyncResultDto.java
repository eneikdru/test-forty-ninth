package com.eneik.production.dto;

public class TaskSyncResultDto {
    private String taskId;
    private String previousStatus;
    private String newStatus;
    private String githubPrState;
    private boolean updated;
    private String message;

    public TaskSyncResultDto() {
    }

    public TaskSyncResultDto(String taskId, String previousStatus, String newStatus, String githubPrState, boolean updated, String message) {
        this.taskId = taskId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.githubPrState = githubPrState;
        this.updated = updated;
        this.message = message;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getGithubPrState() {
        return githubPrState;
    }

    public void setGithubPrState(String githubPrState) {
        this.githubPrState = githubPrState;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
