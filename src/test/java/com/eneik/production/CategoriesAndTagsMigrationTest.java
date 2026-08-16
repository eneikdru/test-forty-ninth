package com.eneik.production;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class CategoriesAndTagsMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testTablesAndIndicesExist() {
        // Query H2 system tables to check if created tables exist
        List<String> expectedTables = List.of("CATEGORIES", "TAGS", "MATERIAL_CATEGORIES", "MATERIAL_TAGS");
        for (String tableName : expectedTables) {
            Integer tableCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?",
                    Integer.class,
                    tableName
            );
            assertEquals(1, tableCount, "Table " + tableName + " must exist after migration");
        }

        // Verify key indexes exist
        List<Map<String, Object>> indexList = jdbcTemplate.queryForList(
                "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_NAME IN ('CATEGORIES', 'TAGS', 'MATERIAL_CATEGORIES', 'MATERIAL_TAGS')"
        );

        List<String> indexNames = indexList.stream()
                .map(m -> (String) m.get("INDEX_NAME"))
                .map(String::toUpperCase)
                .toList();

        assertTrue(indexNames.contains("IDX_CATEGORIES_NAME"), "Index IDX_CATEGORIES_NAME must exist");
        assertTrue(indexNames.contains("IDX_TAGS_NAME"), "Index IDX_TAGS_NAME must exist");
        assertTrue(indexNames.contains("IDX_MATERIAL_CATEGORIES_CATEGORY_ID"), "Index IDX_MATERIAL_CATEGORIES_CATEGORY_ID must exist");
        assertTrue(indexNames.contains("IDX_MATERIAL_TAGS_TAG_ID"), "Index IDX_MATERIAL_TAGS_TAG_ID must exist");
    }

    @Test
    public void testCascadingDeletionNoOrphans() {
        // Insert test category and tag
        jdbcTemplate.update("INSERT INTO categories (name, description) VALUES ('Virology', 'Virology docs')");
        UUID categoryId = jdbcTemplate.queryForObject("SELECT id FROM categories WHERE name = 'Virology'", UUID.class);

        jdbcTemplate.update("INSERT INTO tags (name) VALUES ('surveillance')");
        UUID tagId = jdbcTemplate.queryForObject("SELECT id FROM tags WHERE name = 'surveillance'", UUID.class);

        // Dummy material id
        Long materialId = 9999L;

        // Link material with category and tag
        jdbcTemplate.update("INSERT INTO material_categories (material_id, category_id) VALUES (?, ?)", materialId, categoryId);
        jdbcTemplate.update("INSERT INTO material_tags (material_id, tag_id) VALUES (?, ?)", materialId, tagId);

        // Verify links exist
        Integer catLinkCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM material_categories WHERE category_id = ?", Integer.class, categoryId);
        Integer tagLinkCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM material_tags WHERE tag_id = ?", Integer.class, tagId);
        assertEquals(1, catLinkCount);
        assertEquals(1, tagLinkCount);

        // Delete category and tag and verify cascading deletion in junction tables (no orphan join data)
        jdbcTemplate.update("DELETE FROM categories WHERE id = ?", categoryId);
        jdbcTemplate.update("DELETE FROM tags WHERE id = ?", tagId);

        Integer remainingCatLinks = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM material_categories WHERE category_id = ?", Integer.class, categoryId);
        Integer remainingTagLinks = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM material_tags WHERE tag_id = ?", Integer.class, tagId);
        assertEquals(0, remainingCatLinks, "Material category link must be removed on category deletion");
        assertEquals(0, remainingTagLinks, "Material tag link must be removed on tag deletion");
    }

    @Test
    @DirtiesContext
    public void testRollbackProcedureScriptExecution() {
        // Execute manual undo / rollback statements
        jdbcTemplate.execute("DROP TABLE IF EXISTS material_tags");
        jdbcTemplate.execute("DROP TABLE IF EXISTS material_categories");
        jdbcTemplate.execute("DROP TABLE IF EXISTS tags");
        jdbcTemplate.execute("DROP TABLE IF EXISTS categories");

        // Verify tables are removed cleanly
        List<String> removedTables = List.of("CATEGORIES", "TAGS", "MATERIAL_CATEGORIES", "MATERIAL_TAGS");
        for (String tableName : removedTables) {
            Integer tableCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?",
                    Integer.class,
                    tableName
            );
            assertEquals(0, tableCount, "Table " + tableName + " must be removed after rollback");
        }
    }
}
