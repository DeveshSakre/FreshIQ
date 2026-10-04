"""
Automated Test Suite: Banana Ripeness Classifier & Regression Integrity (Phase 2K.1)
=====================================================================================
Validates:
1. Manifest split counts (272 train, 56 val, 56 test).
2. Class balance across all splits (exactly 3 classes: 0, 1, 2).
3. Zero biological sequence cluster leakage across splits.
4. Checkpoint integrity and independent model reloading.
5. Model output tensor shape (batch, 3).
6. Softmax probability output properties (finite, in [0, 1], sum == 1.0).
7. Inference engine functionality across representative images.
8. Original source dataset preservation (all 66 folders intact with 8 images).
9. Regression verification: Avocado and Mango model checkpoints remain byte-identical.
"""

import os
import hashlib
from pathlib import Path
import pandas as pd
import numpy as np
import torch

from banana_classifier import BananaClassifierEngine, BANANA_EXPECTED_SHA256


BANANA_ROOT = Path(r"D:\FreshIQ\ml\datasets\multi_produce\banana")
MANIFEST_PATH = BANANA_ROOT / "manifests" / "banana_cluster_split_manifest.csv"
SAVED_MODELS_DIR = Path(r"D:\FreshIQ\ml\saved_models")

AVOCADO_CKPT = SAVED_MODELS_DIR / "freshiq_mobilenetv3_avocado_best.pth"
MANGO_CKPT = SAVED_MODELS_DIR / "freshiq_mobilenetv3_mango_best.pth"
BANANA_CKPT = SAVED_MODELS_DIR / "freshiq_mobilenetv3_banana_best.pth"

EXPECTED_AVOCADO_SHA256 = "8aaff3e354937dbc4f06e7b7ec944168d8f56572e3003b5fd833d2155d8f312e"
EXPECTED_MANGO_SHA256 = "4a8a35c2d18142a9c8a577ed51a83101b51cf510f847af23cd9fa9381857cac9"


def compute_sha256(path: Path) -> str:
    hasher = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(65536), b""):
            hasher.update(chunk)
    return hasher.hexdigest()


def test_manifest_splits_and_class_counts():
    assert MANIFEST_PATH.exists(), f"Missing manifest: {MANIFEST_PATH}"
    df = pd.read_csv(MANIFEST_PATH)

    df_tr = df[df["split"] == "train"]
    df_va = df[df["split"] == "val"]
    df_te = df[df["split"] == "test"]

    assert len(df_tr) == 272, f"Expected 272 train samples, got {len(df_tr)}"
    assert len(df_va) == 56, f"Expected 56 val samples, got {len(df_va)}"
    assert len(df_te) == 56, f"Expected 56 test samples, got {len(df_te)}"

    tr_c = df_tr["class_id"].value_counts().to_dict()
    va_c = df_va["class_id"].value_counts().to_dict()
    te_c = df_te["class_id"].value_counts().to_dict()

    assert tr_c == {0: 102, 1: 102, 2: 68}, f"Unexpected train class counts: {tr_c}"
    assert va_c == {0: 21, 1: 21, 2: 14}, f"Unexpected val class counts: {va_c}"
    assert te_c == {0: 21, 1: 21, 2: 14}, f"Unexpected test class counts: {te_c}"


def test_zero_cluster_leakage():
    df = pd.read_csv(MANIFEST_PATH)
    tr_clusters = set(df[df["split"] == "train"]["cluster_id"])
    va_clusters = set(df[df["split"] == "val"]["cluster_id"])
    te_clusters = set(df[df["split"] == "test"]["cluster_id"])

    assert len(tr_clusters.intersection(va_clusters)) == 0, "Train and Val share clusters!"
    assert len(tr_clusters.intersection(te_clusters)) == 0, "Train and Test share clusters!"
    assert len(va_clusters.intersection(te_clusters)) == 0, "Val and Test share clusters!"


def test_banana_checkpoint_exists_and_matches_sha256():
    assert BANANA_CKPT.exists(), f"Banana checkpoint missing at {BANANA_CKPT}"
    actual_hash = compute_sha256(BANANA_CKPT)
    assert actual_hash == BANANA_EXPECTED_SHA256, f"Hash mismatch: expected {BANANA_EXPECTED_SHA256}, got {actual_hash}"


def test_model_reload_and_tensor_shape():
    engine = BananaClassifierEngine(checkpoint_path=BANANA_CKPT)
    assert engine.verify_checkpoint_integrity() is True

    dummy_input = torch.randn(2, 3, 224, 224).to(engine.device)
    with torch.no_grad():
        out = engine.model(dummy_input)
    assert out.shape == (2, 3), f"Expected shape (2, 3), got {out.shape}"


def test_probability_output_properties():
    engine = BananaClassifierEngine(checkpoint_path=BANANA_CKPT)
    # Test with dummy input
    dummy_input = torch.randn(5, 3, 224, 224).to(engine.device)
    with torch.no_grad():
        logits = engine.model(dummy_input)
        probs = torch.softmax(logits, dim=1).cpu().numpy()

    assert np.all(np.isfinite(probs)), "Probabilities contain non-finite values"
    assert np.all((probs >= 0.0) & (probs <= 1.0)), "Probabilities outside [0, 1]"
    assert np.all(np.isclose(np.sum(probs, axis=1), 1.0, atol=1e-5)), "Probabilities do not sum to 1"


def test_inference_on_real_samples():
    engine = BananaClassifierEngine(checkpoint_path=BANANA_CKPT)
    df = pd.read_csv(MANIFEST_PATH)

    # Test one sample from each class
    for target_class in [0, 1, 2]:
        sample_row = df[(df["split"] == "test") & (df["class_id"] == target_class)].iloc[0]
        img_path = BANANA_ROOT / sample_row["path"].replace("/", os.sep)
        assert img_path.exists(), f"Image path does not exist: {img_path}"

        result = engine.predict(img_path)
        assert result["predicted_class_id"] in [0, 1, 2]
        assert result["predicted_class_name"] in ["Unripe", "Semi-ripe", "Ripe"]
        assert 0.0 <= result["confidence"] <= 1.0
        assert len(result["probabilities"]) == 3
        assert "recommendation" in result
        assert "disclaimer" in result


def test_original_dataset_preservation():
    # Verify all 66 nominal folders still contain exactly 8 images
    for i in range(1, 67):
        folder_path = BANANA_ROOT / f"Banana_ID_{i:03d}"
        assert folder_path.is_dir(), f"Missing folder: {folder_path}"
        files = [f for f in os.listdir(folder_path) if f.lower().endswith((".jpg", ".png"))]
        assert len(files) == 8, f"{folder_path} has {len(files)} files, expected 8"


def test_regression_avocado_and_mango_checkpoints_unmodified():
    # Step 21: Verify Avocado and Mango checkpoints were not mutated
    assert AVOCADO_CKPT.exists(), f"Avocado checkpoint missing at {AVOCADO_CKPT}"
    avocado_hash = compute_sha256(AVOCADO_CKPT)
    assert avocado_hash == EXPECTED_AVOCADO_SHA256, (
        f"REGRESSION DETECTED: Avocado checkpoint hash changed! Expected {EXPECTED_AVOCADO_SHA256}, got {avocado_hash}"
    )

    assert MANGO_CKPT.exists(), f"Mango checkpoint missing at {MANGO_CKPT}"
    mango_hash = compute_sha256(MANGO_CKPT)
    assert mango_hash == EXPECTED_MANGO_SHA256, (
        f"REGRESSION DETECTED: Mango checkpoint hash changed! Expected {EXPECTED_MANGO_SHA256}, got {mango_hash}"
    )


if __name__ == "__main__":
    print("Running Banana Model & Regression Test Suite...")
    test_manifest_splits_and_class_counts()
    print("[PASS] test_manifest_splits_and_class_counts")
    test_zero_cluster_leakage()
    print("[PASS] test_zero_cluster_leakage")
    test_banana_checkpoint_exists_and_matches_sha256()
    print("[PASS] test_banana_checkpoint_exists_and_matches_sha256")
    test_model_reload_and_tensor_shape()
    print("[PASS] test_model_reload_and_tensor_shape")
    test_probability_output_properties()
    print("[PASS] test_probability_output_properties")
    test_inference_on_real_samples()
    print("[PASS] test_inference_on_real_samples")
    test_original_dataset_preservation()
    print("[PASS] test_original_dataset_preservation")
    test_regression_avocado_and_mango_checkpoints_unmodified()
    print("[PASS] test_regression_avocado_and_mango_checkpoints_unmodified")
    print("\nALL 8 MODEL & REGRESSION TESTS PASSED SUCCESSFULLY!")
