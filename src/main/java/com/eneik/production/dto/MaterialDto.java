package com.eneik.production.dto;

import com.eneik.production.models.persistence.MaterialEntity;
import java.time.OffsetDateTime;

public class MaterialDto {

    private Long id;
    private String title;
    private String description;
    private String content;
    private String fileName;
    private String contentType;
    private OffsetDateTime createdAt;

    public MaterialDto() {
    }

    public MaterialDto(Long id, String title, String description, String content, String fileName, String contentType, OffsetDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.content = content;
        this.fileName = fileName;
        this.contentType = contentType;
        this.createdAt = createdAt;
    }

    public static MaterialDto fromEntity(MaterialEntity entity) {
        if (entity == null) {
            return null;
        }
        return new MaterialDto(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getContent(),
                entity.getFileName(),
                entity.getContentType(),
                entity.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
