package com.eneik.production;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EpidemiologicalProtocolContractTest {

    @Test
    public void testOpenApiContractFileExistsAndContainsRequiredEndpoints() throws Exception {
        File contractFile = new File("docs/contracts/epidemiological-protocols.openapi.yaml");
        assertTrue(contractFile.exists(), "Contract file docs/contracts/epidemiological-protocols.openapi.yaml must exist");

        String content = Files.readString(contractFile.toPath());

        assertTrue(content.contains("openapi: 3.0.3"), "Contract must be OpenAPI 3.0.3");
        assertTrue(content.contains("/protocols/search"), "Contract must define /protocols/search path");
        assertTrue(content.contains("/protocols/{id}"), "Contract must define /protocols/{id} path");
        assertTrue(content.contains("/protocols:"), "Contract must define /protocols path");

        // Verify required schema components
        assertTrue(content.contains("EpidemiologicalProtocolSummary:"), "Contract must contain EpidemiologicalProtocolSummary schema");
        assertTrue(content.contains("EpidemiologicalProtocolDetail:"), "Contract must contain EpidemiologicalProtocolDetail schema");
        assertTrue(content.contains("EpidemiologicalProtocolCreateRequest:"), "Contract must contain EpidemiologicalProtocolCreateRequest schema");
        assertTrue(content.contains("EpidemiologicalProtocolUpdateRequest:"), "Contract must contain EpidemiologicalProtocolUpdateRequest schema");
        assertTrue(content.contains("EpidemiologicalProtocolSearchResult:"), "Contract must contain EpidemiologicalProtocolSearchResult schema");
        assertTrue(content.contains("ErrorResponse:"), "Contract must contain ErrorResponse schema");
    }
}
