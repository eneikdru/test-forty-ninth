package com.eneik.production.dto;

import java.util.List;

public class EpidemiologicalProtocolSearchResult {

    private List<EpidemiologicalProtocolDto> items;
    private PaginationMeta pagination;

    public EpidemiologicalProtocolSearchResult() {
    }

    public EpidemiologicalProtocolSearchResult(List<EpidemiologicalProtocolDto> items, PaginationMeta pagination) {
        this.items = items;
        this.pagination = pagination;
    }

    public List<EpidemiologicalProtocolDto> getItems() {
        return items;
    }

    public void setItems(List<EpidemiologicalProtocolDto> items) {
        this.items = items;
    }

    public PaginationMeta getPagination() {
        return pagination;
    }

    public void setPagination(PaginationMeta pagination) {
        this.pagination = pagination;
    }
}
