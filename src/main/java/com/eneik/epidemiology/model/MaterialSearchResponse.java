package com.eneik.epidemiology.model;

import java.util.List;
import java.util.Objects;

public class MaterialSearchResponse {
    private List<EpidemiologicalMaterial> items;
    private PageMetadata page;

    public MaterialSearchResponse() {
    }

    public MaterialSearchResponse(List<EpidemiologicalMaterial> items, PageMetadata page) {
        this.items = items;
        this.page = page;
    }

    public List<EpidemiologicalMaterial> getItems() {
        return items;
    }

    public void setItems(List<EpidemiologicalMaterial> items) {
        this.items = items;
    }

    public PageMetadata getPage() {
        return page;
    }

    public void setPage(PageMetadata page) {
        this.page = page;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaterialSearchResponse response = (MaterialSearchResponse) o;
        return Objects.equals(items, response.items) && Objects.equals(page, response.page);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, page);
    }
}
