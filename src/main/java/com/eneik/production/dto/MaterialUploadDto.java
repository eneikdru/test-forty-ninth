package com.eneik.production.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class MaterialUploadDto {

    private String title;
    private String description;
    private String content;
    private String category;
    private List<String> tags;
    private MultipartFile file;

    public MaterialUploadDto() {
    }

    public MaterialUploadDto(String title, String description, String content, String category, List<String> tags, MultipartFile file) {
        this.title = title;
        this.description = description;
        this.content = content;
        this.category = category;
        this.tags = tags;
        this.file = file;
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

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
