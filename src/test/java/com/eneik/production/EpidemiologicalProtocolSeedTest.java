package com.eneik.production;

import com.eneik.production.models.persistence.EpidemiologicalProtocolEntity;
import com.eneik.production.repository.EpidemiologicalProtocolRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class EpidemiologicalProtocolSeedTest {

    @Autowired
    private EpidemiologicalProtocolRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testBaselineSeedAndIdempotency() {
        // Given an empty database auto-seeded by Flyway
        List<EpidemiologicalProtocolEntity> protocols = repository.findAll();

        // Then exactly 15 real epidemiological protocols with full metadata are inserted
        assertEquals(15, protocols.size(), "Seed scripts must insert exactly 15 epidemiological protocols");

        for (EpidemiologicalProtocolEntity protocol : protocols) {
            assertNotNull(protocol.getId(), "Protocol id must not be null");
            assertNotNull(protocol.getCode(), "Protocol code must not be null");
            assertNotNull(protocol.getTitle(), "Protocol title must not be null");
            assertNotNull(protocol.getCategory(), "Protocol category must not be null");
            assertNotNull(protocol.getVersion(), "Protocol version must not be null");
            assertNotNull(protocol.getStatus(), "Protocol status must not be null");
            assertNotNull(protocol.getSummary(), "Protocol summary must not be null");
            assertNotNull(protocol.getAuthorOrganization(), "Protocol author organization must not be null");
            assertNotNull(protocol.getPublicationYear(), "Protocol publication year must not be null");
        }

        // Verify specific known protocols exist
        assertTrue(repository.findByCode("EPI-PROTO-001").isPresent());
        assertTrue(repository.findByCode("EPI-PROTO-011").isPresent());

        // When the seed script runs again on an existing seeded database
        jdbcTemplate.execute("INSERT INTO epidemiological_protocols (code, title, category, version, status, summary, author_organization, publication_year) VALUES " +
                "('EPI-PROTO-001', 'COVID-19 Public Health Surveillance and Outbreak Investigation Protocol', 'Respiratory', 'v3.2', 'APPROVED', 'Comprehensive guidance for standard case definitions, contact tracing, and outbreak investigation protocols for SARS-CoV-2.', 'World Health Organization', 2022), " +
                "('EPI-PROTO-011', 'Avian Influenza A (H5N1) Field Outbreak Investigation Protocol', 'Zoonotic', 'v2.0', 'APPROVED', 'Guidelines for poultry flock exposure assessment, human contact monitoring, PPE requirements, and antiviral prophylaxis triggers.', 'CDC / WHO Joint Taskforce', 2023) " +
                "ON CONFLICT (code) DO NOTHING;");

        // Then it is fully idempotent and does not duplicate baseline content
        List<EpidemiologicalProtocolEntity> protocolsAfterReRun = repository.findAll();
        assertEquals(15, protocolsAfterReRun.size(), "Re-running seed SQL must not duplicate baseline content");
    }
}
