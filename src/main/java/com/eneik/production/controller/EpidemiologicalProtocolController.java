package com.eneik.production.controller;

import com.eneik.production.dto.ErrorResponse;
import com.eneik.production.dto.EpidemiologicalProtocolDto;
import com.eneik.production.dto.EpidemiologicalProtocolRequestDto;
import com.eneik.production.dto.FieldErrorDto;
import com.eneik.production.service.EpidemiologicalProtocolService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/protocols")
public class EpidemiologicalProtocolController {

    private final EpidemiologicalProtocolService protocolService;

    public EpidemiologicalProtocolController(EpidemiologicalProtocolService protocolService) {
        this.protocolService = protocolService;
    }

    @GetMapping
    public ResponseEntity<List<EpidemiologicalProtocolDto>> searchProtocols(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "status", required = false) String status) {
        List<EpidemiologicalProtocolDto> results = protocolService.searchProtocols(query, category, status);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/search")
    public ResponseEntity<List<EpidemiologicalProtocolDto>> searchEndpoint(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "status", required = false) String status) {
        List<EpidemiologicalProtocolDto> results = protocolService.searchProtocols(query, category, status);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EpidemiologicalProtocolDto> getProtocolById(@PathVariable("id") Long id) {
        return protocolService.getProtocolById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<EpidemiologicalProtocolDto> getProtocolByCode(@PathVariable("code") String code) {
        return protocolService.getProtocolByCode(code)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createProtocol(
            @RequestBody EpidemiologicalProtocolRequestDto requestDto,
            HttpServletRequest request) {
        List<FieldErrorDto> fieldErrors = new ArrayList<>();
        if (requestDto == null || requestDto.getCode() == null || requestDto.getCode().trim().isEmpty()) {
            fieldErrors.add(new FieldErrorDto("code", "Code is required"));
        }
        if (requestDto == null || requestDto.getTitle() == null || requestDto.getTitle().trim().isEmpty()) {
            fieldErrors.add(new FieldErrorDto("title", "Title is required"));
        }

        if (!fieldErrors.isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    "VALIDATION_ERROR",
                    "Invalid protocol request payload",
                    request.getRequestURI(),
                    fieldErrors
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        try {
            EpidemiologicalProtocolDto created = protocolService.createProtocol(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    "BAD_REQUEST",
                    ex.getMessage(),
                    request.getRequestURI(),
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProtocol(
            @PathVariable("id") Long id,
            @RequestBody EpidemiologicalProtocolRequestDto requestDto,
            HttpServletRequest request) {
        Optional<EpidemiologicalProtocolDto> updated = protocolService.updateProtocol(id, requestDto);
        if (updated.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated.get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProtocol(@PathVariable("id") Long id) {
        boolean deleted = protocolService.deleteProtocol(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "BAD_REQUEST",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
