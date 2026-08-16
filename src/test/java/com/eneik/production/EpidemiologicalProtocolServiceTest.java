package com.eneik.production;

import com.eneik.production.dto.EpidemiologicalProtocolDto;
import com.eneik.production.dto.EpidemiologicalProtocolRequestDto;
import com.eneik.production.models.persistence.EpidemiologicalProtocolEntity;
import com.eneik.production.repository.EpidemiologicalProtocolRepository;
import com.eneik.production.service.EpidemiologicalProtocolService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EpidemiologicalProtocolServiceTest {

    @Mock
    private EpidemiologicalProtocolRepository repository;

    @InjectMocks
    private EpidemiologicalProtocolService service;

    private EpidemiologicalProtocolEntity p1;
    private EpidemiologicalProtocolEntity p2;

    @BeforeEach
    void setUp() {
        p1 = new EpidemiologicalProtocolEntity(
                "EPI-001",
                "COVID-19 Surveillance Protocol",
                "Respiratory",
                "v3.2",
                "APPROVED",
                "Standard surveillance guidelines for COVID-19",
                "WHO",
                2022
        );
        p1.setId(1L);

        p2 = new EpidemiologicalProtocolEntity(
                "EPI-002",
                "Cholera Early Warning Protocol",
                "Enteric",
                "v2.1",
                "APPROVED",
                "Detection and response for cholera",
                "CDC",
                2021
        );
        p2.setId(2L);
    }

    @Test
    void testSearchProtocolsWithQueryAndFilter() {
        when(repository.searchByKeyword("COVID")).thenReturn(List.of(p1));

        List<EpidemiologicalProtocolDto> results = service.searchProtocols("COVID", "Respiratory", "APPROVED");

        assertEquals(1, results.size());
        assertEquals("EPI-001", results.get(0).getCode());
        assertEquals("COVID-19 Surveillance Protocol", results.get(0).getTitle());
    }

    @Test
    void testSearchProtocolsWithoutQueryReturnsAllFiltered() {
        when(repository.findAll()).thenReturn(List.of(p1, p2));

        List<EpidemiologicalProtocolDto> results = service.searchProtocols(null, "Enteric", null);

        assertEquals(1, results.size());
        assertEquals("EPI-002", results.get(0).getCode());
    }

    @Test
    void testGetProtocolById() {
        when(repository.findById(1L)).thenReturn(Optional.of(p1));

        Optional<EpidemiologicalProtocolDto> result = service.getProtocolById(1L);

        assertTrue(result.isPresent());
        assertEquals("EPI-001", result.get().getCode());
    }

    @Test
    void testCreateProtocolSuccess() {
        EpidemiologicalProtocolRequestDto request = new EpidemiologicalProtocolRequestDto(
                "EPI-003",
                "Ebola Protocol",
                "Viral",
                "v1.0",
                "DRAFT",
                "Ebola response guidelines",
                "MSF",
                2025
        );

        when(repository.findByCode("EPI-003")).thenReturn(Optional.empty());
        when(repository.save(any(EpidemiologicalProtocolEntity.class))).thenAnswer(invocation -> {
            EpidemiologicalProtocolEntity entity = invocation.getArgument(0);
            entity.setId(3L);
            return entity;
        });

        EpidemiologicalProtocolDto created = service.createProtocol(request);

        assertNotNull(created);
        assertEquals(3L, created.getId());
        assertEquals("EPI-003", created.getCode());
        assertEquals("Ebola Protocol", created.getTitle());
    }

    @Test
    void testCreateProtocolDuplicateCodeThrowsException() {
        EpidemiologicalProtocolRequestDto request = new EpidemiologicalProtocolRequestDto(
                "EPI-001",
                "Duplicate Code Protocol",
                "Respiratory",
                "v1.0",
                "DRAFT",
                "Test",
                "WHO",
                2022
        );

        when(repository.findByCode("EPI-001")).thenReturn(Optional.of(p1));

        assertThrows(IllegalArgumentException.class, () -> service.createProtocol(request));
    }

    @Test
    void testDeleteProtocol() {
        when(repository.existsById(1L)).thenReturn(true);

        boolean deleted = service.deleteProtocol(1L);

        assertTrue(deleted);
        verify(repository).deleteById(1L);
    }
}
