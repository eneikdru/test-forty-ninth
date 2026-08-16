package com.eneik.production;

import com.eneik.production.dto.TaskSyncResultDto;
import com.eneik.production.models.persistence.InternalTaskEntity;
import com.eneik.production.repository.InternalTaskRepository;
import com.eneik.production.service.GitHubPrClient;
import com.eneik.production.service.TaskStateSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TaskStateSyncServiceTest {

    private InternalTaskRepository taskRepository;
    private GitHubPrClient gitHubPrClient;
    private TaskStateSyncService syncService;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        taskRepository = mock(InternalTaskRepository.class);
        gitHubPrClient = mock(GitHubPrClient.class);
        fixedClock = Clock.fixed(Instant.parse("2026-08-16T10:00:00Z"), ZoneId.of("UTC"));
        syncService = new TaskStateSyncService(taskRepository, gitHubPrClient, fixedClock);
    }

    @Test
    @DisplayName("When internal task is done but GitHub PR is OPEN, status is reconciled to IN_PROGRESS via atomic update")
    void syncTaskState_whenTaskDoneAndPrOpen_reconcilesToInProgress() {
        InternalTaskEntity task = new InternalTaskEntity(
                "dc09037e-cbf1-4e7e-a5a9-17f9c294ba71",
                "Fix state sync issue",
                "done",
                101,
                "OPEN",
                OffsetDateTime.now(fixedClock),
                OffsetDateTime.now(fixedClock)
        );

        when(taskRepository.findById("dc09037e-cbf1-4e7e-a5a9-17f9c294ba71")).thenReturn(Optional.of(task));
        when(gitHubPrClient.getPullRequestState(101)).thenReturn("OPEN");
        when(taskRepository.updateStatusAtomically(eq("dc09037e-cbf1-4e7e-a5a9-17f9c294ba71"), eq("done"), eq("IN_PROGRESS"), eq("OPEN"), any()))
                .thenReturn(1);

        TaskSyncResultDto result = syncService.syncTaskState("dc09037e-cbf1-4e7e-a5a9-17f9c294ba71");

        assertTrue(result.isUpdated());
        assertEquals("done", result.getPreviousStatus());
        assertEquals("IN_PROGRESS", result.getNewStatus());
        assertEquals("OPEN", result.getGithubPrState());
        verify(taskRepository).updateStatusAtomically(eq("dc09037e-cbf1-4e7e-a5a9-17f9c294ba71"), eq("done"), eq("IN_PROGRESS"), eq("OPEN"), any());
    }

    @Test
    @DisplayName("When internal task is done but GitHub PR is DRAFT, status is reconciled to IN_PROGRESS")
    void syncTaskState_whenTaskDoneAndPrDraft_reconcilesToInProgress() {
        InternalTaskEntity task = new InternalTaskEntity(
                "dc09037e-cbf1-4e7e-a5a9-17f9c294ba71",
                "Draft PR task",
                "done",
                103,
                "DRAFT",
                OffsetDateTime.now(fixedClock),
                OffsetDateTime.now(fixedClock)
        );

        when(taskRepository.findById("dc09037e-cbf1-4e7e-a5a9-17f9c294ba71")).thenReturn(Optional.of(task));
        when(gitHubPrClient.getPullRequestState(103)).thenReturn("DRAFT");
        when(taskRepository.updateStatusAtomically(eq("dc09037e-cbf1-4e7e-a5a9-17f9c294ba71"), eq("done"), eq("IN_PROGRESS"), eq("DRAFT"), any()))
                .thenReturn(1);

        TaskSyncResultDto result = syncService.syncTaskState("dc09037e-cbf1-4e7e-a5a9-17f9c294ba71");

        assertTrue(result.isUpdated());
        assertEquals("done", result.getPreviousStatus());
        assertEquals("IN_PROGRESS", result.getNewStatus());
        assertEquals("DRAFT", result.getGithubPrState());
    }

    @Test
    @DisplayName("When internal task is done but GitHub PR is CLOSED, status is reconciled to BLOCKED")
    void syncTaskState_whenTaskDoneAndPrClosed_reconcilesToBlocked() {
        InternalTaskEntity task = new InternalTaskEntity(
                "dc09037e-cbf1-4e7e-a5a9-17f9c294ba71",
                "Closed PR task",
                "done",
                104,
                "CLOSED",
                OffsetDateTime.now(fixedClock),
                OffsetDateTime.now(fixedClock)
        );

        when(taskRepository.findById("dc09037e-cbf1-4e7e-a5a9-17f9c294ba71")).thenReturn(Optional.of(task));
        when(gitHubPrClient.getPullRequestState(104)).thenReturn("CLOSED");
        when(taskRepository.updateStatusAtomically(eq("dc09037e-cbf1-4e7e-a5a9-17f9c294ba71"), eq("done"), eq("BLOCKED"), eq("CLOSED"), any()))
                .thenReturn(1);

        TaskSyncResultDto result = syncService.syncTaskState("dc09037e-cbf1-4e7e-a5a9-17f9c294ba71");

        assertTrue(result.isUpdated());
        assertEquals("BLOCKED", result.getNewStatus());
        assertEquals("CLOSED", result.getGithubPrState());
    }

    @Test
    @DisplayName("When internal task is done and GitHub PR is MERGED, no status change is made")
    void syncTaskState_whenTaskDoneAndPrMerged_noChange() {
        InternalTaskEntity task = new InternalTaskEntity(
                "dc09037e-cbf1-4e7e-a5a9-17f9c294ba71",
                "Merged PR task",
                "done",
                105,
                "MERGED",
                OffsetDateTime.now(fixedClock),
                OffsetDateTime.now(fixedClock)
        );

        when(taskRepository.findById("dc09037e-cbf1-4e7e-a5a9-17f9c294ba71")).thenReturn(Optional.of(task));
        when(gitHubPrClient.getPullRequestState(105)).thenReturn("MERGED");

        TaskSyncResultDto result = syncService.syncTaskState("dc09037e-cbf1-4e7e-a5a9-17f9c294ba71");

        assertFalse(result.isUpdated());
        assertEquals("done", result.getNewStatus());
        assertEquals("MERGED", result.getGithubPrState());
        verify(taskRepository, never()).updateStatusAtomically(anyString(), anyString(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("When internal task is done but GitHub PR is MISSING, status is reconciled to STUCK")
    void syncTaskState_whenTaskDoneAndPrMissing_reconcilesToStuck() {
        InternalTaskEntity task = new InternalTaskEntity(
                "dc09037e-cbf1-4e7e-a5a9-17f9c294ba71",
                "Orphaned task",
                "done",
                null,
                "MISSING",
                OffsetDateTime.now(fixedClock),
                OffsetDateTime.now(fixedClock)
        );

        when(taskRepository.findById("dc09037e-cbf1-4e7e-a5a9-17f9c294ba71")).thenReturn(Optional.of(task));
        when(gitHubPrClient.getPullRequestState(null)).thenReturn("MISSING");
        when(taskRepository.updateStatusAtomically(eq("dc09037e-cbf1-4e7e-a5a9-17f9c294ba71"), eq("done"), eq("STUCK"), eq("MISSING"), any()))
                .thenReturn(1);

        TaskSyncResultDto result = syncService.syncTaskState("dc09037e-cbf1-4e7e-a5a9-17f9c294ba71");

        assertTrue(result.isUpdated());
        assertEquals("STUCK", result.getNewStatus());
    }

    @Test
    @DisplayName("When atomic update query fails due to concurrent status change, result indicates no update was committed")
    void syncTaskState_whenAtomicUpdateFails_returnsNotUpdated() {
        InternalTaskEntity task = new InternalTaskEntity(
                "dc09037e-cbf1-4e7e-a5a9-17f9c294ba71",
                "Concurrent update task",
                "done",
                102,
                "OPEN",
                OffsetDateTime.now(fixedClock),
                OffsetDateTime.now(fixedClock)
        );

        when(taskRepository.findById("dc09037e-cbf1-4e7e-a5a9-17f9c294ba71")).thenReturn(Optional.of(task));
        when(gitHubPrClient.getPullRequestState(102)).thenReturn("OPEN");
        // Simulate atomic condition failure (0 rows updated)
        when(taskRepository.updateStatusAtomically(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(0);

        TaskSyncResultDto result = syncService.syncTaskState("dc09037e-cbf1-4e7e-a5a9-17f9c294ba71");

        assertFalse(result.isUpdated());
        assertEquals("done", result.getNewStatus());
        assertTrue(result.getMessage().contains("Atomic update failed"));
    }
}
