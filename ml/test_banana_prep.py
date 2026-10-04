"""
Test Banana Dataset Preparation Pipeline (Phase 2K)
===================================================
Automated integrity test suite verifying Step 15 requirements:
1. All manifest paths exist on disk.
2. All computed SHA-256 hashes match on-disk file bytes.
3. No split contains the same biological sequence cluster in another split.
4. No exact SHA-256 duplicate crosses splits.
5. Class IDs are strictly in {0, 1, 2} and match author stage guidelines.
6. Every record has a valid split ('train', 'val', 'test').
7. Manifests are internally consistent with known audit counts.
"""

import os
import hashlib
import pandas as pd
import unittest


BANANA_ROOT = r"D:\FreshIQ\ml\datasets\multi_produce\banana"
MANIFESTS_DIR = os.path.join(BANANA_ROOT, "manifests")


def get_sha256(filepath: str) -> str:
    hasher = hashlib.sha256()
    with open(filepath, "rb") as f:
        for chunk in iter(lambda: f.read(65536), b""):
            hasher.update(chunk)
    return hasher.hexdigest()


def test_manifest_files_exist():
    verified_path = os.path.join(MANIFESTS_DIR, "banana_verified_manifest.csv")
    dedup_path = os.path.join(MANIFESTS_DIR, "banana_deduplicated_manifest.csv")
    split_path = os.path.join(MANIFESTS_DIR, "banana_cluster_split_manifest.csv")

    assert os.path.exists(verified_path), f"Missing {verified_path}"
    assert os.path.exists(dedup_path), f"Missing {dedup_path}"
    assert os.path.exists(split_path), f"Missing {split_path}"


def test_verified_manifest_counts():
    df_verified = pd.read_csv(os.path.join(MANIFESTS_DIR, "banana_verified_manifest.csv"))
    assert len(df_verified) == 528, f"Expected 528 records, found {len(df_verified)}"
    assert df_verified["sha256"].nunique() == 264, f"Expected 264 unique hashes, found {df_verified['sha256'].nunique()}"
    assert df_verified["cluster_id"].nunique() == 48, f"Expected 48 unique clusters, found {df_verified['cluster_id'].nunique()}"
    assert df_verified["nominal_id"].nunique() == 66, f"Expected 66 nominal folders, found {df_verified['nominal_id'].nunique()}"


def test_all_image_paths_exist_and_hashes_valid():
    df_verified = pd.read_csv(os.path.join(MANIFESTS_DIR, "banana_verified_manifest.csv"))
    for idx, row in df_verified.iterrows():
        abs_path = os.path.join(BANANA_ROOT, row["path"].replace("/", os.sep))
        assert os.path.exists(abs_path), f"File does not exist: {abs_path}"
        # Verify hash matches disk content
        actual_hash = get_sha256(abs_path)
        assert actual_hash == row["sha256"], f"Hash mismatch at {abs_path}: expected {row['sha256']}, got {actual_hash}"


def test_class_ids_and_names():
    for mname in ["banana_verified_manifest.csv", "banana_deduplicated_manifest.csv", "banana_cluster_split_manifest.csv"]:
        df = pd.read_csv(os.path.join(MANIFESTS_DIR, mname))
        valid_classes = {0, 1, 2}
        assert set(df["class_id"].unique()).issubset(valid_classes), f"Invalid class IDs in {mname}: {df['class_id'].unique()}"
        
        # Verify mapping: 0 -> Unripe, 1 -> Semi-ripe, 2 -> Ripe
        for _, row in df.iterrows():
            if row["day"] <= 2:
                assert row["class_id"] == 0 and row["class_name"] == "Unripe"
            elif row["day"] <= 5:
                assert row["class_id"] == 1 and row["class_name"] == "Semi-ripe"
            else:
                assert row["class_id"] == 2 and row["class_name"] == "Ripe"


def test_no_cluster_overlap_across_splits():
    df_split = pd.read_csv(os.path.join(MANIFESTS_DIR, "banana_cluster_split_manifest.csv"))
    valid_splits = {"train", "val", "test"}
    assert set(df_split["split"].unique()) == valid_splits, f"Unexpected split values: {df_split['split'].unique()}"

    train_clusters = set(df_split[df_split["split"] == "train"]["cluster_id"])
    val_clusters = set(df_split[df_split["split"] == "val"]["cluster_id"])
    test_clusters = set(df_split[df_split["split"] == "test"]["cluster_id"])

    assert len(train_clusters.intersection(val_clusters)) == 0, "Leakage: train & val share clusters!"
    assert len(train_clusters.intersection(test_clusters)) == 0, "Leakage: train & test share clusters!"
    assert len(val_clusters.intersection(test_clusters)) == 0, "Leakage: val & test share clusters!"


def test_no_exact_sha256_duplicates_across_splits():
    # Test both in the deduplicated split manifest and in the full verified manifest with splits assigned
    for mname in ["banana_cluster_split_manifest.csv", "banana_verified_manifest.csv"]:
        df = pd.read_csv(os.path.join(MANIFESTS_DIR, mname))
        train_hashes = set(df[df["split"] == "train"]["sha256"])
        val_hashes = set(df[df["split"] == "val"]["sha256"])
        test_hashes = set(df[df["split"] == "test"]["sha256"])

        assert len(train_hashes.intersection(val_hashes)) == 0, f"Leakage in {mname}: train & val share SHA-256 hashes!"
        assert len(train_hashes.intersection(test_hashes)) == 0, f"Leakage in {mname}: train & test share SHA-256 hashes!"
        assert len(val_hashes.intersection(test_hashes)) == 0, f"Leakage in {mname}: val & test share SHA-256 hashes!"


def test_deduplicated_manifest_structure():
    df_dedup = pd.read_csv(os.path.join(MANIFESTS_DIR, "banana_deduplicated_manifest.csv"))
    # 48 unique clusters * 8 days = 384 images
    assert len(df_dedup) == 384, f"Expected 384 deduplicated images, found {len(df_dedup)}"
    assert df_dedup["cluster_id"].nunique() == 48, f"Expected 48 unique clusters, found {df_dedup['cluster_id'].nunique()}"
    
    # Check split cluster distribution
    train_c = df_dedup[df_dedup["split"] == "train"]["cluster_id"].nunique()
    val_c = df_dedup[df_dedup["split"] == "val"]["cluster_id"].nunique()
    test_c = df_dedup[df_dedup["split"] == "test"]["cluster_id"].nunique()
    assert train_c == 34, f"Expected 34 train clusters, got {train_c}"
    assert val_c == 7, f"Expected 7 val clusters, got {val_c}"
    assert test_c == 7, f"Expected 7 test clusters, got {test_c}"


def test_original_dataset_remains_untouched():
    # Verify all 66 original folders still contain exactly 8 images
    for i in range(1, 67):
        folder_path = os.path.join(BANANA_ROOT, f"Banana_ID_{i:03d}")
        assert os.path.isdir(folder_path), f"Folder missing: {folder_path}"
        files = [f for f in os.listdir(folder_path) if f.lower().endswith((".jpg", ".png"))]
        assert len(files) == 8, f"{folder_path} has {len(files)} files, expected 8"


if __name__ == "__main__":
    import sys
    print("Running Banana Dataset Preparation Verification Tests...")
    test_manifest_files_exist()
    print("[PASS] test_manifest_files_exist")
    test_verified_manifest_counts()
    print("[PASS] test_verified_manifest_counts")
    test_all_image_paths_exist_and_hashes_valid()
    print("[PASS] test_all_image_paths_exist_and_hashes_valid")
    test_class_ids_and_names()
    print("[PASS] test_class_ids_and_names")
    test_no_cluster_overlap_across_splits()
    print("[PASS] test_no_cluster_overlap_across_splits")
    test_no_exact_sha256_duplicates_across_splits()
    print("[PASS] test_no_exact_sha256_duplicates_across_splits")
    test_deduplicated_manifest_structure()
    print("[PASS] test_deduplicated_manifest_structure")
    test_original_dataset_remains_untouched()
    print("[PASS] test_original_dataset_remains_untouched")
    print("\nALL 8 PREPARATION VERIFICATION TESTS PASSED SUCCESSFULLY!")
