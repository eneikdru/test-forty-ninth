package com.eneik.epidemiology.model;

import java.util.Objects;

public class MaterialDocumentContent {
    private String materialId;
    private String fileName;
    private String contentType;
    private byte[] content;

    public MaterialDocumentContent() {
    }

    public MaterialDocumentContent(String materialId, String fileName, String contentType, byte[] content) {
        this.materialId = materialId;
        this.fileName = fileName;
        this.contentType = contentType;
        this.content = content;
    }

    public String getMaterialId() {
        return materialId;
    }

    public void setMaterialId(String materialId) {
        this.materialId = materialId;
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

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
