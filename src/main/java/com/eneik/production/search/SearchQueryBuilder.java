package com.eneik.production.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SearchQueryBuilder {

    private String query;
    private String category;
    private String contentType;
    private final List<String> tags = new ArrayList<>();
    private final Map<String, String> metadataFilters = new HashMap<>();

    public SearchQueryBuilder() {
    }

    public SearchQueryBuilder withQuery(String query) {
        this.query = query != null ? query.trim() : null;
        return this;
    }

    public SearchQueryBuilder withCategory(String category) {
        this.category = category != null ? category.trim() : null;
        return this;
    }

    public SearchQueryBuilder withContentType(String contentType) {
        this.contentType = contentType != null ? contentType.trim() : null;
        return this;
    }

    public SearchQueryBuilder withTag(String tag) {
        if (tag != null && !tag.isBlank()) {
            this.tags.add(tag.trim());
        }
        return this;
    }

    public SearchQueryBuilder withTags(List<String> tags) {
        if (tags != null) {
            for (String tag : tags) {
                withTag(tag);
            }
        }
        return this;
    }

    public SearchQueryBuilder withMetadataFilter(String key, String value) {
        if (key != null && !key.isBlank() && value != null) {
            this.metadataFilters.put(key.trim(), value.trim());
        }
        return this;
    }

    public SearchQuery build() {
        return new SearchQuery(query, category, contentType, tags, metadataFilters);
    }

    public static class SearchQuery {
        private final String query;
        private final String category;
        private final String contentType;
        private final List<String> tags;
        private final Map<String, String> metadataFilters;

        public SearchQuery(String query, String category, String contentType, List<String> tags, Map<String, String> metadataFilters) {
            this.query = query;
            this.category = category;
            this.contentType = contentType;
            this.tags = Collections.unmodifiableList(new ArrayList<>(tags));
            this.metadataFilters = Collections.unmodifiableMap(new HashMap<>(metadataFilters));
        }

        public String getQuery() {
            return query;
        }

        public String getCategory() {
            return category;
        }

        public String getContentType() {
            return contentType;
        }

        public List<String> getTags() {
            return tags;
        }

        public Map<String, String> getMetadataFilters() {
            return metadataFilters;
        }

        public boolean hasFilters() {
            return (category != null && !category.isBlank())
                    || (contentType != null && !contentType.isBlank())
                    || !tags.isEmpty()
                    || !metadataFilters.isEmpty();
        }

        public boolean hasTextQuery() {
            return query != null && !query.isBlank();
        }

        public String buildJpqlWhereClause() {
            List<String> clauses = new ArrayList<>();
            if (hasTextQuery()) {
                clauses.add("(LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(m.description) LIKE LOWER(CONCAT('%', :query, '%')))");
            }
            if (category != null && !category.isBlank()) {
                clauses.add("m.category = :category");
            }
            if (contentType != null && !contentType.isBlank()) {
                clauses.add("m.contentType = :contentType");
            }
            if (clauses.isEmpty()) {
                return "1=1";
            }
            return String.join(" AND ", clauses);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SearchQuery that = (SearchQuery) o;
            return Objects.equals(query, that.query) &&
                   Objects.equals(category, that.category) &&
                   Objects.equals(contentType, that.contentType) &&
                   Objects.equals(tags, that.tags) &&
                   Objects.equals(metadataFilters, that.metadataFilters);
        }

        @Override
        public int hashCode() {
            return Objects.hash(query, category, contentType, tags, metadataFilters);
        }
    }
}
