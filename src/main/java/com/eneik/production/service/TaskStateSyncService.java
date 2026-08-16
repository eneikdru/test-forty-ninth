package com.eneik.production.service;

import com.eneik.production.dto.TaskSyncResultDto;
import com.eneik.production.models.persistence.InternalTaskEntity;
import com.eneik.production.repository.InternalTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TaskStateSyncService {

    private final InternalTaskRepository taskRepository;
    private final GitHubPrClient gitHubPrClient;
    private final Clock clock;

    @Autowired
    public TaskStateSyncService(InternalTaskRepository taskRepository, GitHubPrClient gitHubPrClient) {
        this(taskRepository, gitHubPrClient, Clock.systemUTC());
    }

    public TaskStateSyncService(InternalTaskRepository taskRepository, GitHubPrClient gitHubPrClient, Clock clock) {
        this.taskRepository = taskRepository;
        this.gitHubPrClient = gitHubPrClient;
        this.clock = clock;
    }

    @Transactional
    public TaskSyncResultDto syncTaskState(String taskId) {
        Optional<InternalTaskEntity> optionalTask = taskRepository.findById(taskId);
        if (optionalTask.isEmpty()) {
            return new TaskSyncResultDto(taskId, null, null, null, false, "Task not found");
        }

        InternalTaskEntity task = optionalTask.get();
        return syncSingleTask(task);
    }

    @Transactional
    public List<TaskSyncResultDto> syncAllTasksState() {
        List<InternalTaskEntity> tasks = taskRepository.findAll();
        List<TaskSyncResultDto> results = new ArrayList<>();
        for (InternalTaskEntity task : tasks) {
            results.add(syncSingleTask(task));
        }
        return results;
    }

    private TaskSyncResultDto syncSingleTask(InternalTaskEntity task) {
        String currentStatus = task.getStatus();
        Integer prNumber = task.getGithubPrNumber();
        String prState = gitHubPrClient.getPullRequestState(prNumber);

        String targetStatus = null;

        if ("OPEN".equalsIgnoreCase(prState) || "DRAFT".equalsIgnoreCase(prState)) {
            targetStatus = "IN_PROGRESS";
        } else if ("MERGED".equalsIgnoreCase(prState)) {
            targetStatus = "done";
        } else if ("CLOSED".equalsIgnoreCase(prState)) {
            targetStatus = "BLOCKED";
        } else if ("MISSING".equalsIgnoreCase(prState)) {
            targetStatus = "STUCK";
        }

        if (targetStatus != null && !targetStatus.equalsIgnoreCase(currentStatus)) {
            LocalDateTime now = LocalDateTime.now(clock);
            // Execute atomically-guarded database update query using current status guard
            int rowsUpdated = taskRepository.updateStatusAtomically(task.getId(), currentStatus, targetStatus, prState, now);
            if (rowsUpdated > 0) {
                return new TaskSyncResultDto(
                        task.getId(),
                        currentStatus,
                        targetStatus,
                        prState,
                        true,
                        "Updated task status from " + currentStatus + " to " + targetStatus + " matching GitHub PR state " + prState
                );
            } else {
                return new TaskSyncResultDto(
                        task.getId(),
                        currentStatus,
                        currentStatus,
                        prState,
                        false,
                        "Atomic update failed: status changed concurrently"
                );
            }
        }

        return new TaskSyncResultDto(task.getId(), currentStatus, currentStatus, prState, false, "State is in sync or no discrepancy action required");
    }
}
