package com.eneik.production.dto;

import com.eneik.production.models.persistence.MaterialEntity;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MaterialDto {

    private Long id;
    private String title;
    private String description;
    private String content;
    private String fileName;
    private String contentType;
    private String category;
    private List<String> tags;
    private LocalDateTime createdAt;

    public MaterialDto() {
    }

    public MaterialDto(Long id, String title, String description, String content, String fileName, String contentType, LocalDateTime createdAt) {
        this(id, title, description, content, fileName, contentType, null, Collections.emptyList(), createdAt);
    }

    public MaterialDto(Long id, String title, String description, String content, String fileName, String contentType, String category, List<String> tags, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.content = content;
        this.fileName = fileName;
        this.contentType = contentType;
        this.category = category;
        this.tags = tags != null ? tags : Collections.emptyList();
        this.createdAt = createdAt;
    }

    public static MaterialDto fromEntity(MaterialEntity entity) {
        if (entity == null) {
            return null;
        }
        List<String> tagList = Collections.emptyList();
        if (entity.getTags() != null && !entity.getTags().isBlank()) {
            tagList = Arrays.stream(entity.getTags().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
        return new MaterialDto(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getContent(),
                entity.getFileName(),
                entity.getContentType(),
                entity.getCategory(),
                tagList,
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
