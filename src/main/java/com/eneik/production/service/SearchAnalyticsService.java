package com.eneik.production.service;

import com.eneik.production.dto.SearchEventRequestDTO;
import com.eneik.production.dto.SearchMetricsDTO;
import com.eneik.production.models.persistence.SearchAnalyticsEventEntity;
import com.eneik.production.repository.SearchAnalyticsEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SearchAnalyticsService {

    private final SearchAnalyticsEventRepository repository;

    public SearchAnalyticsService(SearchAnalyticsEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SearchAnalyticsEventEntity recordSearchEvent(SearchEventRequestDTO dto) {
        SearchAnalyticsEventEntity entity = new SearchAnalyticsEventEntity(
                dto.getQuery(),
                dto.getUserId(),
                dto.getFilters(),
                dto.getResultCount() != null ? dto.getResultCount() : 0,
                dto.getExecutionTimeMs() != null ? dto.getExecutionTimeMs() : 0L
        );
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public SearchMetricsDTO getAggregateMetrics() {
        List<SearchAnalyticsEventEntity> events = repository.findAll();
        long totalSearches = events.size();
        if (totalSearches == 0) {
            return new SearchMetricsDTO(0, 0.0, 0, 0.0);
        }

        double totalExecTime = 0;
        long zeroResultCount = 0;

        for (SearchAnalyticsEventEntity event : events) {
            if (event.getExecutionTimeMs() != null) {
                totalExecTime += event.getExecutionTimeMs();
            }
            if (event.getResultCount() != null && event.getResultCount() == 0) {
                zeroResultCount++;
            }
        }

        double avgExecTime = totalExecTime / totalSearches;
        long uniqueUsers = events.stream()
                .map(SearchAnalyticsEventEntity::getUserId)
                .filter(userId -> userId != null && !userId.isBlank())
                .distinct()
                .count();

        double zeroResultRate = (double) zeroResultCount / totalSearches;

        return new SearchMetricsDTO(totalSearches, avgExecTime, uniqueUsers, zeroResultRate);
    }
}
