package com.eneik.production.repository;

import com.eneik.production.models.persistence.SearchAnalyticsEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface SearchAnalyticsEventRepository extends JpaRepository<SearchAnalyticsEventEntity, Long> {

    List<SearchAnalyticsEventEntity> findByUserId(String userId);

    List<SearchAnalyticsEventEntity> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end);

    @Query("SELECT AVG(s.executionTimeMs) FROM SearchAnalyticsEventEntity s")
    Double getAverageExecutionTimeMs();
}
