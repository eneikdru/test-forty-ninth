package com.eneik.production.dto;

public class PaginationMeta {

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean isFirst;
    private boolean isLast;

    public PaginationMeta() {
    }

    public PaginationMeta(int page, int size, long totalElements, int totalPages, boolean isFirst, boolean isLast) {
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.isFirst = isFirst;
        this.isLast = isLast;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isIsFirst() {
        return isFirst;
    }

    public void setIsFirst(boolean first) {
        isFirst = first;
    }

    public boolean isIsLast() {
        return isLast;
    }

    public void setIsLast(boolean last) {
        isLast = last;
    }
}
