CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS material_categories (
    material_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (material_id, category_id),
    CONSTRAINT fk_material_categories_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS material_tags (
    material_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (material_id, tag_id),
    CONSTRAINT fk_material_tags_tag FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_categories_name ON categories (name);
CREATE INDEX IF NOT EXISTS idx_tags_name ON tags (name);
CREATE INDEX IF NOT EXISTS idx_material_categories_category_id ON material_categories (category_id);
CREATE INDEX IF NOT EXISTS idx_material_tags_tag_id ON material_tags (tag_id);
