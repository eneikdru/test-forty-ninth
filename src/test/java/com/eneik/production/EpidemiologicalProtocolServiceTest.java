package com.eneik.production;

import com.eneik.production.dto.CreateEpidemiologicalProtocolRequest;
import com.eneik.production.dto.EpidemiologicalProtocolDto;
import com.eneik.production.dto.EpidemiologicalProtocolSearchResult;
import com.eneik.production.dto.UpdateEpidemiologicalProtocolRequest;
import com.eneik.production.models.persistence.EpidemiologicalProtocolEntity;
import com.eneik.production.repository.EpidemiologicalProtocolRepository;
import com.eneik.production.service.EpidemiologicalProtocolService;
import com.eneik.production.service.EpidemiologicalProtocolService.ConcurrentUpdateException;
import com.eneik.production.service.EpidemiologicalProtocolService.DuplicateProtocolCodeException;
import com.eneik.production.service.EpidemiologicalProtocolService.ProtocolNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EpidemiologicalProtocolServiceTest {

    @Autowired
    private EpidemiologicalProtocolRepository repository;

    @Autowired
    private EpidemiologicalProtocolService service;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void testCreateProtocolSuccessAndValidation() {
        CreateEpidemiologicalProtocolRequest req = new CreateEpidemiologicalProtocolRequest(
                "EPI-UNIT-001",
                "Unit Test Protocol",
                "Respiratory",
                "v1.0",
                "DRAFT",
                "Test summary",
                "Test Org",
                2026,
                "PROTOCOL"
        );

        EpidemiologicalProtocolDto created = service.createProtocol(req);
        assertNotNull(created.getId());
        assertEquals("EPI-UNIT-001", created.getCode());
        assertEquals("Unit Test Protocol", created.getTitle());

        // Blank code should throw IllegalArgumentException
        CreateEpidemiologicalProtocolRequest invalidReq = new CreateEpidemiologicalProtocolRequest(
                "   ",
                "Invalid Protocol",
                "Respiratory",
                "v1.0",
                "DRAFT",
                "Test",
                "Org",
                2026,
                "PROTOCOL"
        );
        assertThrows(IllegalArgumentException.class, () -> service.createProtocol(invalidReq));

        // Duplicate code should throw DuplicateProtocolCodeException
        assertThrows(DuplicateProtocolCodeException.class, () -> service.createProtocol(req));
    }

    @Test
    void testSearchProtocolsPaginationAndFiltering() {
        CreateEpidemiologicalProtocolRequest p1 = new CreateEpidemiologicalProtocolRequest(
                "EPI-UNIT-101", "Influenza Surveillance", "Respiratory", "v1.0", "APPROVED", "Flu tracking", "CDC", 2025, "PROTOCOL");
        CreateEpidemiologicalProtocolRequest p2 = new CreateEpidemiologicalProtocolRequest(
                "EPI-UNIT-102", "Dengue Containment", "Vector-Borne", "v2.0", "APPROVED", "Mosquito control", "WHO", 2026, "PROTOCOL");
        CreateEpidemiologicalProtocolRequest p3 = new CreateEpidemiologicalProtocolRequest(
                "EPI-UNIT-103", "Cholera Water Protocol", "Enteric", "v1.0", "DRAFT", "Water testing", "PAHO", 2024, "PROTOCOL");

        service.createProtocol(p1);
        service.createProtocol(p2);
        service.createProtocol(p3);

        // Search by keyword
        EpidemiologicalProtocolSearchResult searchResult = service.searchProtocols("Influenza", null, null, null, 0, 10, "createdAt", "desc");
        assertEquals(1, searchResult.getItems().size());
        assertEquals("EPI-UNIT-101", searchResult.getItems().get(0).getCode());

        // Filter by category
        EpidemiologicalProtocolSearchResult catResult = service.searchProtocols(null, "Vector-Borne", null, null, 0, 10, "createdAt", "desc");
        assertEquals(1, catResult.getItems().size());
        assertEquals("EPI-UNIT-102", catResult.getItems().get(0).getCode());

        // Filter by status
        EpidemiologicalProtocolSearchResult statusResult = service.searchProtocols(null, null, "APPROVED", null, 0, 10, "createdAt", "desc");
        assertEquals(2, statusResult.getItems().size());

        // Invalid pagination arguments
        assertThrows(IllegalArgumentException.class, () -> service.searchProtocols(null, null, null, null, -1, 10, "createdAt", "desc"));
        assertThrows(IllegalArgumentException.class, () -> service.searchProtocols(null, null, null, null, 0, 0, "createdAt", "desc"));
        assertThrows(IllegalArgumentException.class, () -> service.searchProtocols(null, null, null, null, 0, 101, "createdAt", "desc"));
        assertThrows(IllegalArgumentException.class, () -> service.searchProtocols(null, null, null, null, 0, 10, "invalidField", "desc"));
    }

    @Test
    void testUpdateProtocolSuccessAndAtomicallyGuardedStatusConflict() {
        CreateEpidemiologicalProtocolRequest req = new CreateEpidemiologicalProtocolRequest(
                "EPI-UNIT-201",
                "Original Protocol Title",
                "Zoonotic",
                "v1.0",
                "DRAFT",
                "Original summary",
                "Org A",
                2025,
                "PROTOCOL"
        );
        EpidemiologicalProtocolDto created = service.createProtocol(req);

        UpdateEpidemiologicalProtocolRequest updateReq = new UpdateEpidemiologicalProtocolRequest(
                "EPI-UNIT-201",
                "Updated Protocol Title",
                "Zoonotic",
                "v1.1",
                "APPROVED",
                "Updated summary",
                "Org A",
                2026,
                "PROTOCOL"
        );

        EpidemiologicalProtocolDto updated = service.updateProtocol(created.getId(), updateReq);
        assertEquals("Updated Protocol Title", updated.getTitle());
        assertEquals("APPROVED", updated.getStatus());

        // Verify repository status guard update directly for concurrent conflict scenario
        int updatedRows = repository.updateProtocolWithStatusGuard(
                created.getId(),
                "WRONG_EXPECTED_STATUS",
                "EPI-UNIT-201",
                "Conflicting Title Update",
                "Zoonotic",
                "v1.2",
                "APPROVED",
                "Conflicting summary",
                "Org A",
                2026,
                "PROTOCOL"
        );
        assertEquals(0, updatedRows);
    }

    @Test
    void testDeleteProtocolSuccessAndNotFound() {
        CreateEpidemiologicalProtocolRequest req = new CreateEpidemiologicalProtocolRequest(
                "EPI-UNIT-301",
                "Protocol To Delete",
                "Environmental",
                "v1.0",
                "DRAFT",
                "Delete summary",
                "Org B",
                2025,
                "PROTOCOL"
        );
        EpidemiologicalProtocolDto created = service.createProtocol(req);

        service.deleteProtocol(created.getId());

        Optional<EpidemiologicalProtocolDto> fetched = service.getProtocolById(created.getId());
        assertTrue(fetched.isEmpty());

        assertThrows(ProtocolNotFoundException.class, () -> service.deleteProtocol(created.getId()));
    }
}
