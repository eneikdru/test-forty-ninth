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
    @DisplayName("Given a freshly seeded environment, When querying active database, Then baseline protocols are returned")
    public void testInitialContentProvisioningCount() {
        List<EpidemiologicalProtocolEntity> protocols = repository.findAll();
        assertEquals(15, protocols.size(), "Exactly 15 protocols must be returned from the active database after baseline seeding");
    }

    @Test
    @DisplayName("Given search API, When searching for known seed keywords like 'COVID-19' or 'Avian', Then appropriate seed protocols are retrieved")
    public void testSearchKnownSeedKeywords() {
        // Search COVID-19
        List<EpidemiologicalProtocolEntity> covidResults = repository.searchByKeyword("COVID-19");
        assertFalse(covidResults.isEmpty(), "Searching for 'COVID-19' should return results");
        assertTrue(covidResults.stream().anyMatch(p -> "EPI-PROTO-001".equals(p.getCode())), "Results for 'COVID-19' should contain EPI-PROTO-001");

        // Search Cholera
        List<EpidemiologicalProtocolEntity> choleraResults = repository.searchByKeyword("Cholera");
        assertFalse(choleraResults.isEmpty(), "Searching for 'Cholera' should return results");
        assertTrue(choleraResults.stream().anyMatch(p -> "EPI-PROTO-002".equals(p.getCode())), "Results for 'Cholera' should contain EPI-PROTO-002");

        // Search Avian Influenza
        List<EpidemiologicalProtocolEntity> avianResults = repository.searchByKeyword("Avian");
        assertFalse(avianResults.isEmpty(), "Searching for 'Avian' should return results");
        assertTrue(avianResults.stream().anyMatch(p -> "EPI-PROTO-011".equals(p.getCode())), "Results for 'Avian' should contain EPI-PROTO-011");

        // Search Marburg
        List<EpidemiologicalProtocolEntity> marburgResults = repository.searchByKeyword("Marburg");
        assertFalse(marburgResults.isEmpty(), "Searching for 'Marburg' should return results");
        assertTrue(marburgResults.stream().anyMatch(p -> "EPI-PROTO-012".equals(p.getCode())), "Results for 'Marburg' should contain EPI-PROTO-012");
    }
}
