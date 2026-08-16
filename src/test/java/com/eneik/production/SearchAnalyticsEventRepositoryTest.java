package com.eneik.production;

import com.eneik.production.models.persistence.SearchAnalyticsEventEntity;
import com.eneik.production.repository.SearchAnalyticsEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class SearchAnalyticsEventRepositoryTest {

    @Autowired
    private SearchAnalyticsEventRepository repository;

    @Test
    public void testSaveAndQuerySearchAnalyticsEvents() {
        LocalDateTime baseTime = LocalDateTime.of(2026, 8, 16, 10, 0, 0);

        SearchAnalyticsEventEntity event1 = new SearchAnalyticsEventEntity(
                "COVID-19 protocol", "user_1", "{\"category\":\"protocol\"}", 15, 120L
        );
        event1.setCreatedAt(baseTime);

        SearchAnalyticsEventEntity event2 = new SearchAnalyticsEventEntity(
                "Influenza stats", "user_2", "{\"category\":\"stats\"}", 5, 80L
        );
        event2.setCreatedAt(baseTime.plusHours(1));

        SearchAnalyticsEventEntity event3 = new SearchAnalyticsEventEntity(
                "Malaria data", "user_1", "{\"category\":\"data\"}", 0, 200L
        );
        event3.setCreatedAt(baseTime.plusHours(2));

        repository.save(event1);
        repository.save(event2);
        repository.save(event3);

        List<SearchAnalyticsEventEntity> user1Events = repository.findByUserId("user_1");
        assertEquals(2, user1Events.size());

        List<SearchAnalyticsEventEntity> rangeEvents = repository.findByCreatedAtBetween(
                baseTime.minusMinutes(10),
                baseTime.plusMinutes(90)
        );
        assertEquals(2, rangeEvents.size());

        Double avgExecTime = repository.getAverageExecutionTimeMs();
        assertNotNull(avgExecTime);
        assertEquals(133.33333333333334, avgExecTime, 0.0001);
    }
}
