package com.eneik.production;

import com.eneik.production.models.persistence.InternalTaskEntity;
import com.eneik.production.repository.InternalTaskRepository;
import com.eneik.production.service.GitHubPrClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskStateSyncIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InternalTaskRepository taskRepository;

    @MockBean
    private GitHubPrClient gitHubPrClient;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    @DisplayName("Integration test: Synchronize task status when PR is CLOSED reconciles status to BLOCKED")
    void testTaskStateSyncIntegration_prClosed_reconcilesToBlocked() throws Exception {
        String taskId = "ca69a93d-4e7e-4b71-a5a9-17f9c294ba99";
        LocalDateTime now = LocalDateTime.now();
        InternalTaskEntity task = new InternalTaskEntity(taskId, "Closed PR task", "done", 99, "CLOSED", now, now);
        taskRepository.save(task);

        when(gitHubPrClient.getPullRequestState(99)).thenReturn("CLOSED");

        mockMvc.perform(post("/api/tasks/" + taskId + "/sync")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(taskId))
                .andExpect(jsonPath("$.previousStatus").value("done"))
                .andExpect(jsonPath("$.newStatus").value("BLOCKED"))
                .andExpect(jsonPath("$.githubPrState").value("CLOSED"))
                .andExpect(jsonPath("$.updated").value(true));

        InternalTaskEntity updatedTask = taskRepository.findById(taskId).orElseThrow();
        assertEquals("BLOCKED", updatedTask.getStatus());
    }

    @Test
    @DisplayName("Integration test: Synchronize task status using REST API and verify database state and atomic update query")
    void testTaskStateSyncIntegration() throws Exception {
        String taskId = "dc09037e-cbf1-4e7e-a5a9-17f9c294ba71";
        LocalDateTime now = LocalDateTime.now();
        InternalTaskEntity task = new InternalTaskEntity(taskId, "Discrepant task", "done", 42, "OPEN", now, now);
        taskRepository.save(task);

        when(gitHubPrClient.getPullRequestState(42)).thenReturn("OPEN");

        mockMvc.perform(post("/api/tasks/" + taskId + "/sync")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(taskId))
                .andExpect(jsonPath("$.previousStatus").value("done"))
                .andExpect(jsonPath("$.newStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.githubPrState").value("OPEN"))
                .andExpect(jsonPath("$.updated").value(true));

        InternalTaskEntity updatedTask = taskRepository.findById(taskId).orElseThrow();
        assertEquals("IN_PROGRESS", updatedTask.getStatus());
        assertEquals("OPEN", updatedTask.getGithubPrState());
    }

    @Test
    @Transactional
    @DisplayName("Integration test: Verify atomic guarded update query directly on database")
    void testAtomicUpdateQuery() {
        String taskId = "dc09037e-cbf1-4e7e-a5a9-17f9c294ba71";
        LocalDateTime now = LocalDateTime.now();
        InternalTaskEntity task = new InternalTaskEntity(taskId, "Discrepant task", "done", 10, "OPEN", now, now);
        taskRepository.save(task);

        // Atomic update with matching expected status succeeds
        int rowsUpdated = taskRepository.updateStatusAtomically(taskId, "done", "IN_PROGRESS", "OPEN", now.plusMinutes(1));
        assertEquals(1, rowsUpdated);

        // Atomic update with mismatched expected status fails (0 rows updated)
        int staleUpdateRows = taskRepository.updateStatusAtomically(taskId, "done", "BLOCKED", "OPEN", now.plusMinutes(2));
        assertEquals(0, staleUpdateRows);
    }

    @Test
    @DisplayName("Integration test: Synchronize stuck task dc09037e when PR is OPEN reconciles to IN_PROGRESS")
    void testTaskStateSyncIntegration_stuckTask_reconcilesToInProgress() throws Exception {
        String taskId = "dc09037e-cbf1-4e7e-a5a9-17f9c294ba71";
        LocalDateTime now = LocalDateTime.now();
        InternalTaskEntity task = new InternalTaskEntity(taskId, "Stuck candidate task", "STUCK", 108, "OPEN", now, now);
        taskRepository.save(task);

        when(gitHubPrClient.getPullRequestState(108)).thenReturn("OPEN");

        mockMvc.perform(post("/api/tasks/" + taskId + "/sync")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(taskId))
                .andExpect(jsonPath("$.previousStatus").value("STUCK"))
                .andExpect(jsonPath("$.newStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.githubPrState").value("OPEN"))
                .andExpect(jsonPath("$.updated").value(true));

        InternalTaskEntity updatedTask = taskRepository.findById(taskId).orElseThrow();
        assertEquals("IN_PROGRESS", updatedTask.getStatus());
    }

    @Test
    @DisplayName("Integration test: Synchronize all tasks via POST /api/tasks/sync reconciles 'done' tasks with non-merged PR states in database")
    void testBulkTaskStateSyncIntegration() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        InternalTaskEntity task1 = new InternalTaskEntity("2d1cb887-1111-4b71-a5a9-17f9c294ba01", "Desync task 1", "done", 301, "OPEN", now, now);
        InternalTaskEntity task2 = new InternalTaskEntity("6bd5fbaf-2222-4b71-a5a9-17f9c294ba02", "Desync task 2", "done", 302, "CLOSED", now, now);
        taskRepository.save(task1);
        taskRepository.save(task2);

        when(gitHubPrClient.getPullRequestState(301)).thenReturn("OPEN");
        when(gitHubPrClient.getPullRequestState(302)).thenReturn("CLOSED");

        mockMvc.perform(post("/api/tasks/sync")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        InternalTaskEntity updated1 = taskRepository.findById("2d1cb887-1111-4b71-a5a9-17f9c294ba01").orElseThrow();
        InternalTaskEntity updated2 = taskRepository.findById("6bd5fbaf-2222-4b71-a5a9-17f9c294ba02").orElseThrow();

        assertEquals("IN_PROGRESS", updated1.getStatus());
        assertEquals("BLOCKED", updated2.getStatus());
    }
}
