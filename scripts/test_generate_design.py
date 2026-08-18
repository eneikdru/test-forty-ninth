#!/usr/bin/env python3
import os
import shutil
import tempfile
import unittest
from generate_design import generate_html_design, DEFAULT_MODEL, DISALLOWED_MODELS

class TestGenerateDesign(unittest.TestCase):

    def setUp(self):
        self.test_dir = tempfile.mkdtemp()

    def tearDown(self):
        shutil.rmtree(self.test_dir, ignore_errors=True)

    def test_default_model_is_html_capable(self):
        self.assertNotIn(DEFAULT_MODEL, DISALLOWED_MODELS)
        self.assertIn("flash", DEFAULT_MODEL)

    def test_generate_html_mockup_creates_mockup_html(self):
        output_path = generate_html_design(
            prompt="Test Protocol Dashboard",
            model=DEFAULT_MODEL,
            output_dir=self.test_dir,
            seed=42
        )
        self.assertTrue(os.path.exists(output_path))
        self.assertTrue(output_path.endswith("mockup.html"))

        with open(output_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertIn("<!DOCTYPE html>", content)
        self.assertIn("Test Protocol Dashboard", content)
        self.assertIn(DEFAULT_MODEL, content)

    def test_disallowed_image_models_raise_error(self):
        for image_model in DISALLOWED_MODELS:
            with self.subTest(model=image_model):
                with self.assertRaises(ValueError) as ctx:
                    generate_html_design(
                        prompt="Test UI",
                        model=image_model,
                        output_dir=self.test_dir
                    )
                self.assertIn("image-only model", str(ctx.exception))

if __name__ == "__main__":
    unittest.main()
