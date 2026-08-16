-- =============================================================================
-- MANUAL DISASTER RECOVERY / ROLLBACK INSTRUCTIONS
-- Version: U20260816054204525
-- Description: Rollback script for V20260816054204525__create_categories_and_tags_schema.sql
-- Note: Flyway Community Edition does not automatically execute undo scripts.
-- This file serves as a verified manual recovery script.
-- =============================================================================

DROP TABLE IF EXISTS material_tags;
DROP TABLE IF EXISTS material_categories;
DROP TABLE IF EXISTS tags;
DROP TABLE IF EXISTS categories;
