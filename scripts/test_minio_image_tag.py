#!/usr/bin/env python3
import os
import re
import unittest

class TestMinioImageTag(unittest.TestCase):

    def setUp(self):
        self.repo_root = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
        self.compose_path = os.path.join(self.repo_root, "docker-compose.yml")

    def test_docker_compose_exists(self):
        self.assertTrue(os.path.exists(self.compose_path), "docker-compose.yml should exist")

    def test_minio_image_tag_is_resolvable_and_not_broken(self):
        with open(self.compose_path, "r", encoding="utf-8") as f:
            content = f.read()

        # Extract image tag for object-storage service
        match = re.search(r'object-storage:.*?image:\s*([^\s]+)', content, re.DOTALL)
        self.assertIsNotNone(match, "Could not find image tag for object-storage service in docker-compose.yml")

        image_tag = match.group(1).strip()
        self.assertTrue(image_tag.startswith("minio/minio:"), f"Expected minio/minio image, got {image_tag}")

        broken_tag = "minio/minio:RELEASE.2023-09-20T22-40-07Z"
        self.assertNotEqual(image_tag, broken_tag, f"Image tag must not be unresolvable tag {broken_tag}")

        # Ensure image tag matches valid RELEASE pattern
        self.assertRegex(image_tag, r'^minio/minio:RELEASE\.\d{4}-\d{2}-\d{2}T\d{2}-\d{2}-\d{2}Z$')

if __name__ == "__main__":
    unittest.main()
