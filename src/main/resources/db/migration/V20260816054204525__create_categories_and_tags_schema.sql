CREATE ALIAS IF NOT EXISTS gen_random_uuid FOR "java.util.UUID.randomUUID";

CREATE TABLE categories (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tags (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE material_categories (
    material_id BIGINT NOT NULL,
    category_id UUID NOT NULL,
    PRIMARY KEY (material_id, category_id),
    CONSTRAINT fk_material_categories_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE
);

CREATE TABLE material_tags (
    material_id BIGINT NOT NULL,
    tag_id UUID NOT NULL,
    PRIMARY KEY (material_id, tag_id),
    CONSTRAINT fk_material_tags_tag FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
);

CREATE INDEX idx_categories_name ON categories (name);
CREATE INDEX idx_tags_name ON tags (name);
CREATE INDEX idx_material_categories_category_id ON material_categories (category_id);
CREATE INDEX idx_material_tags_tag_id ON material_tags (tag_id);
