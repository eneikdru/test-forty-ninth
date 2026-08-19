ALTER TABLE materials ADD COLUMN category VARCHAR(255);

CREATE TABLE material_tag_names (
    material_id BIGINT NOT NULL,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (material_id, tag),
    CONSTRAINT fk_material_tag_names_material FOREIGN KEY (material_id) REFERENCES materials (id) ON DELETE CASCADE
);

CREATE INDEX idx_materials_category ON materials (category);
