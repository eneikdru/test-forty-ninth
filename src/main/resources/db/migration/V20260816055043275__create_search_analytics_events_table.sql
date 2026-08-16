CREATE TABLE search_analytics_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    query VARCHAR(512) NOT NULL,
    user_id VARCHAR(100),
    filters VARCHAR(1024),
    result_count INT NOT NULL DEFAULT 0,
    execution_time_ms BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_search_events_created_at ON search_analytics_events (created_at);
CREATE INDEX idx_search_events_query ON search_analytics_events (query);
CREATE INDEX idx_search_events_user_id ON search_analytics_events (user_id);
