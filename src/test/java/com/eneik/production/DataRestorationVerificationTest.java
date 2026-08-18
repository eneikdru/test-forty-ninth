package com.eneik.production;

import com.eneik.production.models.persistence.EpidemiologicalProtocolEntity;
import com.eneik.production.models.persistence.SearchAnalyticsEventEntity;
import com.eneik.production.repository.EpidemiologicalProtocolRepository;
import com.eneik.production.repository.SearchAnalyticsEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class DataRestorationVerificationTest {

    @Autowired
    private EpidemiologicalProtocolRepository protocolRepository;

    @Autowired
    private SearchAnalyticsEventRepository analyticsRepository;

    @Test
    public void testFullRestoreAndCriticalWorkflowVerification(@TempDir Path tempDir) throws IOException, InterruptedException {
        // Given an isolated staging environment and sample object storage documents
        Path backupDir = tempDir.resolve("backup_snapshots");
        Path sourceObjectStorageDir = tempDir.resolve("source_object_storage");
        Path restoredObjectStorageDir = tempDir.resolve("restored_object_storage");
        Path alertLogFile = tempDir.resolve("alerts.log");

        Files.createDirectories(sourceObjectStorageDir);
        Path sampleDoc = sourceObjectStorageDir.resolve("EPI_REPORT_2026_001.pdf");
        String sampleContent = "CONFIDENTIAL: Epidemiological Outbreak Analysis and Intervention Report 2026";
        Files.writeString(sampleDoc, sampleContent);

        // 1. Execute Backup Procedure
        ProcessBuilder backupPb = new ProcessBuilder("./scripts/backup.sh");
        backupPb.environment().put("BACKUP_DIR", backupDir.toString());
        backupPb.environment().put("OBJECT_STORAGE_DIR", sourceObjectStorageDir.toString());
        backupPb.environment().put("ALERT_LOG_FILE", alertLogFile.toString());
        backupPb.environment().put("SIMULATE_FAILURE", "false");

        Process backupProcess = backupPb.start();
        int backupExitCode = backupProcess.waitFor();
        assertEquals(0, backupExitCode, "Backup script must complete successfully");

        // Locate snapshot directory
        File[] snapshotDirs = backupDir.toFile().listFiles(File::isDirectory);
        assertTrue(snapshotDirs != null && snapshotDirs.length > 0, "Snapshot directory should be created");
        File snapshotDir = snapshotDirs[0];

        // 2. Execute Restore Procedure into Isolated Environment
        ProcessBuilder restorePb = new ProcessBuilder("./scripts/restore.sh");
        restorePb.environment().put("SNAPSHOT_DIR", snapshotDir.getAbsolutePath());
        restorePb.environment().put("OBJECT_STORAGE_DIR", restoredObjectStorageDir.toString());
        restorePb.environment().put("ALERT_LOG_FILE", alertLogFile.toString());
        restorePb.environment().put("SIMULATE_FAILURE", "false");

        Process restoreProcess = restorePb.start();
        int restoreExitCode = restoreProcess.waitFor();
        assertEquals(0, restoreExitCode, "Restore script must complete successfully");

        // 3. Verify Document Storage Recovery without Data Corruption
        Path restoredDoc = restoredObjectStorageDir.resolve("EPI_REPORT_2026_001.pdf");
        assertTrue(Files.exists(restoredDoc), "Restored document must exist in target object storage");
        String restoredContent = Files.readString(restoredDoc);
        assertEquals(sampleContent, restoredContent, "Restored document content must match original exactly without corruption");

        // 4. Validate Critical Application Workflows Post-Restoration
        // Workflow A: Query Epidemiological Protocols
        List<EpidemiologicalProtocolEntity> protocols = protocolRepository.findAll();
        assertTrue(protocols.size() >= 10, "Restored database must contain pre-snapshot epidemiological protocols");

        Optional<EpidemiologicalProtocolEntity> specificProtocol = protocolRepository.findByCode("EPI-PROTO-001");
        assertTrue(specificProtocol.isPresent(), "Critical protocol EPI-PROTO-001 must be retrievable");
        assertEquals("Respiratory", specificProtocol.get().getCategory(), "Protocol category must match pre-snapshot state");

        // Workflow B: Create and query new domain entities in restored environment
        SearchAnalyticsEventEntity newEvent = new SearchAnalyticsEventEntity();
        newEvent.setUserId("user-restored-001");
        newEvent.setQuery("cholera outbreak protocol");
        newEvent.setFilters("{\"category\":\"Enteric\"}");
        newEvent.setResultCount(5);
        newEvent.setExecutionTimeMs(42L);
        newEvent.setCreatedAt(OffsetDateTime.now());

        analyticsRepository.save(newEvent);

        List<SearchAnalyticsEventEntity> userEvents = analyticsRepository.findByUserId("user-restored-001");
        assertEquals(1, userEvents.size(), "Newly registered analytics event post-restore must be persisted and searchable");
        assertEquals("cholera outbreak protocol", userEvents.get(0).getQuery());
    }
}
