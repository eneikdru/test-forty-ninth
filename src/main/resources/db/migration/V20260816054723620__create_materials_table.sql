CREATE TABLE materials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    content TEXT,
    file_name VARCHAR(255),
    content_type VARCHAR(100),
    file_data BLOB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_materials_title ON materials (title);
