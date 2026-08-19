package com.eneik.production.controller;

import com.eneik.production.dto.CreateEpidemiologicalProtocolRequest;
import com.eneik.production.dto.EpidemiologicalProtocolDto;
import com.eneik.production.dto.EpidemiologicalProtocolSearchResult;
import com.eneik.production.dto.ErrorResponse;
import com.eneik.production.dto.UpdateEpidemiologicalProtocolRequest;
import com.eneik.production.service.EpidemiologicalProtocolService;
import com.eneik.production.service.EpidemiologicalProtocolService.ConcurrentUpdateException;
import com.eneik.production.service.EpidemiologicalProtocolService.DuplicateProtocolCodeException;
import com.eneik.production.service.EpidemiologicalProtocolService.ProtocolNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/protocols")
public class EpidemiologicalProtocolController {

    private final EpidemiologicalProtocolService protocolService;

    public EpidemiologicalProtocolController(EpidemiologicalProtocolService protocolService) {
        this.protocolService = protocolService;
    }

    @GetMapping
    public ResponseEntity<EpidemiologicalProtocolSearchResult> searchProtocols(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "recordType", required = false) String recordType,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = "desc") String sortOrder) {

        EpidemiologicalProtocolSearchResult result = protocolService.searchProtocols(
                q, category, status, recordType, page, size, sortBy, sortOrder);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<EpidemiologicalProtocolDto> createProtocol(
            @RequestBody CreateEpidemiologicalProtocolRequest request) {

        EpidemiologicalProtocolDto created = protocolService.createProtocol(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EpidemiologicalProtocolDto> getProtocolById(@PathVariable("id") Long id) {
        EpidemiologicalProtocolDto dto = protocolService.getProtocolById(id)
                .orElseThrow(() -> new ProtocolNotFoundException("Epidemiological protocol with ID '" + id + "' was not found"));
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EpidemiologicalProtocolDto> updateProtocol(
            @PathVariable("id") Long id,
            @RequestBody UpdateEpidemiologicalProtocolRequest request) {

        EpidemiologicalProtocolDto updated = protocolService.updateProtocol(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProtocol(@PathVariable("id") Long id) {
        protocolService.deleteProtocol(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "INVALID_SEARCH_PARAMETER",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(DuplicateProtocolCodeException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateCode(DuplicateProtocolCodeException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "DUPLICATE_PROTOCOL_CODE",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(ProtocolNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ProtocolNotFoundException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "PROTOCOL_NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(ConcurrentUpdateException.class)
    public ResponseEntity<ErrorResponse> handleConcurrentUpdate(ConcurrentUpdateException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "CONCURRENT_UPDATE_CONFLICT",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
}
