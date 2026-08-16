package com.eneik.production.controller;

import com.eneik.production.dto.InternalTaskDto;
import com.eneik.production.dto.TaskSyncResultDto;
import com.eneik.production.models.persistence.InternalTaskEntity;
import com.eneik.production.repository.InternalTaskRepository;
import com.eneik.production.service.TaskStateSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskStateSyncController {

    private final TaskStateSyncService taskStateSyncService;
    private final InternalTaskRepository taskRepository;

    public TaskStateSyncController(TaskStateSyncService taskStateSyncService, InternalTaskRepository taskRepository) {
        this.taskStateSyncService = taskStateSyncService;
        this.taskRepository = taskRepository;
    }

    @PostMapping("/sync")
    public ResponseEntity<List<TaskSyncResultDto>> syncAllTasks() {
        List<TaskSyncResultDto> results = taskStateSyncService.syncAllTasksState();
        return ResponseEntity.ok(results);
    }

    @PostMapping("/{id}/sync")
    public ResponseEntity<TaskSyncResultDto> syncTask(@PathVariable("id") String id) {
        TaskSyncResultDto result = taskStateSyncService.syncTaskState(id);
        if (result.getMessage() != null && result.getMessage().contains("Task not found")) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InternalTaskDto> getTask(@PathVariable("id") String id) {
        return taskRepository.findById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<InternalTaskDto>> getAllTasks() {
        List<InternalTaskDto> dtos = taskRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<InternalTaskDto> createTask(@RequestBody InternalTaskDto dto) {
        LocalDateTime now = LocalDateTime.now();
        InternalTaskEntity entity = new InternalTaskEntity(
                dto.getId(),
                dto.getTitle(),
                dto.getStatus(),
                dto.getGithubPrNumber(),
                dto.getGithubPrState(),
                now,
                now
        );
        InternalTaskEntity saved = taskRepository.save(entity);
        return ResponseEntity.ok(toDto(saved));
    }

    private InternalTaskDto toDto(InternalTaskEntity entity) {
        return new InternalTaskDto(
                entity.getId(),
                entity.getTitle(),
                entity.getStatus(),
                entity.getGithubPrNumber(),
                entity.getGithubPrState(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
