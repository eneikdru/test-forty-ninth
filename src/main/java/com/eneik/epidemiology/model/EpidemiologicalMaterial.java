package com.eneik.epidemiology.model;

import java.time.LocalDate;
import java.util.Objects;

public class EpidemiologicalMaterial {
    private String id;
    private String title;
    private String description;
    private String category;
    private String author;
    private LocalDate publishDate;
    private String fileName;
    private long fileSizeBytes;

    public EpidemiologicalMaterial() {
    }

    public EpidemiologicalMaterial(String id, String title, String description, String category,
                                   String author, LocalDate publishDate, String fileName, long fileSizeBytes) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.author = author;
        this.publishDate = publishDate;
        this.fileName = fileName;
        this.fileSizeBytes = fileSizeBytes;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDate getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDate publishDate) {
        this.publishDate = publishDate;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EpidemiologicalMaterial that = (EpidemiologicalMaterial) o;
        return fileSizeBytes == that.fileSizeBytes &&
                Objects.equals(id, that.id) &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(category, that.category) &&
                Objects.equals(author, that.author) &&
                Objects.equals(publishDate, that.publishDate) &&
                Objects.equals(fileName, that.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, category, author, publishDate, fileName, fileSizeBytes);
    }
}
