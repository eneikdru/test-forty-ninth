package com.eneik.production;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class BackupConfigurationTest {

    @Test
    public void testBackupScriptExistsAndIsExecutable() {
        File backupScript = new File("scripts/backup.sh");
        assertTrue(backupScript.exists(), "scripts/backup.sh must exist");
        assertTrue(backupScript.canExecute(), "scripts/backup.sh must be executable");

        File backupTestScript = new File("scripts/test_backup.sh");
        assertTrue(backupTestScript.exists(), "scripts/test_backup.sh must exist");
        assertTrue(backupTestScript.canExecute(), "scripts/test_backup.sh must be executable");

        File restoreScript = new File("scripts/restore.sh");
        assertTrue(restoreScript.exists(), "scripts/restore.sh must exist");
        assertTrue(restoreScript.canExecute(), "scripts/restore.sh must be executable");

        File restoreTestScript = new File("scripts/test_restore.sh");
        assertTrue(restoreTestScript.exists(), "scripts/test_restore.sh must exist");
        assertTrue(restoreTestScript.canExecute(), "scripts/test_restore.sh must be executable");
    }

    @Test
    public void testDockerfileAndDockerComposeExist() {
        File dockerfile = new File("Dockerfile");
        assertTrue(dockerfile.exists(), "Dockerfile must exist");

        File dockerCompose = new File("docker-compose.yml");
        assertTrue(dockerCompose.exists(), "docker-compose.yml must exist");
    }
}
