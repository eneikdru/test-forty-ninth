package com.eneik.production;

import com.eneik.production.models.persistence.EpidemiologicalProtocolEntity;
import com.eneik.production.repository.EpidemiologicalProtocolRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class EpidemiologicalContentVerificationTest {

    @Autowired
    private EpidemiologicalProtocolRepository repository;

    @Test
    @DisplayName("Given a freshly seeded environment, When querying active database, Then exactly 10 initial protocols are returned")
    public void testInitialContentProvisioningCount() {
        List<EpidemiologicalProtocolEntity> protocols = repository.findAll();
        assertEquals(10, protocols.size(), "Exactly 10 initial protocols must be returned from the active database");
    }

    @Test
    @DisplayName("Given search API, When searching for known seed keywords like 'COVID-19', Then appropriate seed protocols are retrieved")
    public void testSearchKnownSeedKeywords() {
        // Search COVID-19
        List<EpidemiologicalProtocolEntity> covidResults = repository.searchByKeyword("COVID-19");
        assertFalse(covidResults.isEmpty(), "Searching for 'COVID-19' should return results");
        assertTrue(covidResults.stream().anyMatch(p -> "EPI-PROTO-001".equals(p.getCode())), "Results for 'COVID-19' should contain EPI-PROTO-001");

        // Search Cholera
        List<EpidemiologicalProtocolEntity> choleraResults = repository.searchByKeyword("Cholera");
        assertFalse(choleraResults.isEmpty(), "Searching for 'Cholera' should return results");
        assertTrue(choleraResults.stream().anyMatch(p -> "EPI-PROTO-002".equals(p.getCode())), "Results for 'Cholera' should contain EPI-PROTO-002");

        // Search Measles
        List<EpidemiologicalProtocolEntity> measlesResults = repository.searchByKeyword("Measles");
        assertFalse(measlesResults.isEmpty(), "Searching for 'Measles' should return results");
        assertTrue(measlesResults.stream().anyMatch(p -> "EPI-PROTO-004".equals(p.getCode())), "Results for 'Measles' should contain EPI-PROTO-004");

        // Search Dengue
        List<EpidemiologicalProtocolEntity> dengueResults = repository.searchByKeyword("Dengue");
        assertFalse(dengueResults.isEmpty(), "Searching for 'Dengue' should return results");
        assertTrue(dengueResults.stream().anyMatch(p -> "EPI-PROTO-006".equals(p.getCode())), "Results for 'Dengue' should contain EPI-PROTO-006");
    }
}
