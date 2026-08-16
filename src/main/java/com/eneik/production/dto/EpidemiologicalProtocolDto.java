package com.eneik.production.dto;

import com.eneik.production.models.persistence.EpidemiologicalProtocolEntity;
import java.time.LocalDateTime;

public class EpidemiologicalProtocolDto {

    private Long id;
    private String code;
    private String title;
    private String category;
    private String version;
    private String status;
    private String summary;
    private String authorOrganization;
    private Integer publicationYear;
    private LocalDateTime createdAt;

    public EpidemiologicalProtocolDto() {
    }

    public EpidemiologicalProtocolDto(
            Long id,
            String code,
            String title,
            String category,
            String version,
            String status,
            String summary,
            String authorOrganization,
            Integer publicationYear,
            LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.category = category;
        this.version = version;
        this.status = status;
        this.summary = summary;
        this.authorOrganization = authorOrganization;
        this.publicationYear = publicationYear;
        this.createdAt = createdAt;
    }

    public static EpidemiologicalProtocolDto fromEntity(EpidemiologicalProtocolEntity entity) {
        if (entity == null) {
            return null;
        }
        return new EpidemiologicalProtocolDto(
                entity.getId(),
                entity.getCode(),
                entity.getTitle(),
                entity.getCategory(),
                entity.getVersion(),
                entity.getStatus(),
                entity.getSummary(),
                entity.getAuthorOrganization(),
                entity.getPublicationYear(),
                entity.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getAuthorOrganization() {
        return authorOrganization;
    }

    public void setAuthorOrganization(String authorOrganization) {
        this.authorOrganization = authorOrganization;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
