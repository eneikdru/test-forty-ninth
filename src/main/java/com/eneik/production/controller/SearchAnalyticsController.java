package com.eneik.production.controller;

import com.eneik.production.dto.SearchEventRequestDTO;
import com.eneik.production.dto.SearchMetricsDTO;
import com.eneik.production.models.persistence.SearchAnalyticsEventEntity;
import com.eneik.production.service.SearchAnalyticsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics/search")
public class SearchAnalyticsController {

    private final SearchAnalyticsService analyticsService;

    public SearchAnalyticsController(SearchAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @PostMapping("/events")
    public ResponseEntity<SearchAnalyticsEventEntity> recordSearchEvent(@RequestBody SearchEventRequestDTO requestDTO) {
        SearchAnalyticsEventEntity createdEntity = analyticsService.recordSearchEvent(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEntity);
    }

    @GetMapping("/metrics")
    public ResponseEntity<SearchMetricsDTO> getSearchMetrics() {
        SearchMetricsDTO metrics = analyticsService.getAggregateMetrics();
        return ResponseEntity.ok(metrics);
    }
}
