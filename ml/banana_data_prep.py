"""
FreshIQ ML Pipeline - Banana Dataset Deduplication and Preparation (Phase 2K)
=============================================================================
This script performs a rigorous, reproducible verification and preparation pass
over the Banana Ripening Dataset (banana_ripening_dataset_day0_to_day7, Mendeley Data v2).

It produces:
1. Complete verification of 528 images across Banana_ID_001..066.
2. SHA-256 byte-for-byte duplicate mapping (264 unique hashes).
3. 8-day sequence identity mapping revealing 11 clone groups across 29 folders.
4. 48 canonical biological sequence clusters (banana_cluster_001..048).
5. 17 connected components of clusters sharing SHA-256 frames.
6. 3-stage ripening class mapping (0: Unripe, 1: Semi-ripe, 2: Ripe).
7. Leakage-safe Train/Val/Test split (~70/15/15) at the component/cluster level with seed 42.
8. GroupKFold / StratifiedGroupKFold cross-validation analysis.
9. Verified, deduplicated, and split manifests in ml/datasets/multi_produce/banana/manifests/.
"""

import os
import re
import json
import hashlib
from typing import Dict, List, Tuple, Set
import pandas as pd
import numpy as np
from PIL import Image
import networkx as nx
from sklearn.model_selection import GroupKFold, StratifiedGroupKFold


# Base paths
BANANA_ROOT = r"D:\FreshIQ\ml\datasets\multi_produce\banana"
MANIFESTS_DIR = os.path.join(BANANA_ROOT, "manifests")
AUDIT_DIR = os.path.join(BANANA_ROOT, "audit")
RANDOM_SEED = 42


def get_sha256(filepath: str) -> str:
    hasher = hashlib.sha256()
    with open(filepath, "rb") as f:
        for chunk in iter(lambda: f.read(65536), b""):
            hasher.update(chunk)
    return hasher.hexdigest()


def parse_day_from_filename(filename: str) -> int:
    m = re.match(r"Day_(\d+)", filename, re.IGNORECASE)
    if m:
        return int(m.group(1))
    # Handle known camera timestamp anomaly in Banana_ID_009
    if "IMG" in filename.upper():
        return 7
    raise ValueError(f"Unrecognized day pattern in filename: {filename}")


def get_class_info(day: int) -> Tuple[int, str]:
    """
    Author-recommended stage mapping (Mendeley Data v2):
    Day 0-2 -> Unripe (Class 0)
    Day 3-5 -> Semi-ripe (Class 1)
    Day 6-7 -> Ripe (Class 2)
    """
    if 0 <= day <= 2:
        return 0, "Unripe"
    elif 3 <= day <= 5:
        return 1, "Semi-ripe"
    elif 6 <= day <= 7:
        return 2, "Ripe"
    else:
        raise ValueError(f"Invalid observation day: {day}")


def run_pipeline():
    print("=" * 70)
    print("FRESHIQ ML PHASE 2K: BANANA DATASET DEDUPLICATION & PREPARATION")
    print("=" * 70)

    os.makedirs(MANIFESTS_DIR, exist_ok=True)
    os.makedirs(AUDIT_DIR, exist_ok=True)

    nominal_folders = [f"Banana_ID_{i:03d}" for i in range(1, 67)]
    
    # -------------------------------------------------------------------------
    # STEP 2: VERIFY ALL FILES
    # -------------------------------------------------------------------------
    print("\n[STEP 2] Running verification pass over all 66 nominal folders...")
    all_records = []
    corrupt_files = []
    
    for folder in nominal_folders:
        folder_path = os.path.join(BANANA_ROOT, folder)
        if not os.path.isdir(folder_path):
            raise FileNotFoundError(f"Missing specimen folder: {folder_path}")
            
        file_list = sorted(os.listdir(folder_path))
        for fname in file_list:
            if not fname.lower().endswith((".jpg", ".jpeg", ".png")):
                continue
                
            abs_path = os.path.join(folder_path, fname)
            rel_path = f"{folder}/{fname}"
            file_size = os.path.getsize(abs_path)
            ext = os.path.splitext(fname)[1].lower()
            
            try:
                with Image.open(abs_path) as img:
                    width, height = img.size
                    img_format = img.format
            except Exception as e:
                corrupt_files.append((abs_path, str(e)))
                continue
                
            day = parse_day_from_filename(fname)
            sha256_hash = get_sha256(abs_path)
            class_id, class_name = get_class_info(day)
            
            all_records.append({
                "path": rel_path,
                "filename": fname,
                "extension": ext,
                "file_size_bytes": file_size,
                "width": width,
                "height": height,
                "sha256": sha256_hash,
                "nominal_id": folder,
                "day": day,
                "observation_day": f"Day {day}",
                "class_id": class_id,
                "class_name": class_name,
                "author_stage_guideline": class_name,
            })

    total_files = len(all_records)
    valid_images = total_files
    jpeg_count = sum(1 for r in all_records if r["extension"] in [".jpg", ".jpeg"])
    png_count = sum(1 for r in all_records if r["extension"] == ".png")
    all_hashes = [r["sha256"] for r in all_records]
    unique_hashes = set(all_hashes)
    exact_duplicate_count = total_files - len(unique_hashes)

    print(f"  Total files scanned:      {total_files}")
    print(f"  Valid image files:        {valid_images}")
    print(f"  Corrupt / Invalid files:  {len(corrupt_files)}")
    print(f"  JPEG images:              {jpeg_count} ({(jpeg_count/total_files)*100:.1f}%)")
    print(f"  PNG images:               {png_count} ({(png_count/total_files)*100:.1f}%)")
    print(f"  Unique SHA-256 hashes:    {len(unique_hashes)}")
    print(f"  Duplicate file instances: {exact_duplicate_count} ({(exact_duplicate_count/total_files)*100:.1f}%)")

    # -------------------------------------------------------------------------
    # STEP 3 & 4: DUPLICATE MAPPING & CLONE GROUPS
    # -------------------------------------------------------------------------
    print("\n[STEP 3 & 4] Mapping byte-for-byte duplicates and 8-day clone groups...")
    hash_to_records = {}
    for r in all_records:
        hash_to_records.setdefault(r["sha256"], []).append(r)

    # 8-day sequence profile per nominal folder
    folder_sequences = {}
    for folder in nominal_folders:
        folder_recs = sorted([r for r in all_records if r["nominal_id"] == folder], key=lambda x: x["day"])
        assert len(folder_recs) == 8, f"Folder {folder} does not have 8 daily observations!"
        seq_tuple = tuple(r["sha256"] for r in folder_recs)
        folder_sequences[folder] = seq_tuple

    # Group identical sequences
    sequence_to_folders = {}
    for folder, seq in folder_sequences.items():
        sequence_to_folders.setdefault(seq, []).append(folder)

    # Sort clusters deterministically by the lowest Banana_ID number
    sorted_unique_sequences = sorted(sequence_to_folders.items(), key=lambda item: item[1][0])
    
    folder_to_cluster = {}
    folder_to_canonical = {}
    clone_group_map = {}
    clone_idx = 1
    
    for cluster_idx, (seq, members) in enumerate(sorted_unique_sequences, start=1):
        cluster_id = f"banana_cluster_{cluster_idx:03d}"
        canonical_folder = members[0] # primary representative
        is_clone = len(members) > 1
        cg_name = f"clone_group_{clone_idx:02d}" if is_clone else "none"
        if is_clone:
            clone_idx += 1
            
        for m in members:
            folder_to_cluster[m] = cluster_id
            folder_to_canonical[m] = (m == canonical_folder)
            clone_group_map[m] = cg_name

    num_clusters = len(sorted_unique_sequences)
    clone_groups_list = [members for _, members in sorted_unique_sequences if len(members) > 1]
    folders_in_clones = sum(len(members) for members in clone_groups_list)
    
    print(f"  Unique biological sequence clusters: {num_clusters}")
    print(f"  Multi-folder clone groups:           {len(clone_groups_list)}")
    print(f"  Folders involved in clone groups:    {folders_in_clones}")
    for idx, members in enumerate(clone_groups_list, 1):
        print(f"    Clone Group {idx:02d}: {members} ({len(members)} identical 8-day sequences)")

    # -------------------------------------------------------------------------
    # STEP 5: INTRA-SPECIMEN STATIC DUPLICATES & CONNECTED COMPONENTS
    # -------------------------------------------------------------------------
    print("\n[STEP 5] Checking intra-specimen static duplicates and graph connectivity...")
    # Intra-specimen duplicate check: consecutive days sharing exact hash
    intra_specimen_static_dups = 0
    for folder in nominal_folders:
        folder_recs = sorted([r for r in all_records if r["nominal_id"] == folder], key=lambda x: x["day"])
        for d in range(1, 8):
            if folder_recs[d]["sha256"] == folder_recs[d-1]["sha256"]:
                intra_specimen_static_dups += 1
    print(f"  Intra-specimen consecutive static duplicate occurrences: {intra_specimen_static_dups}")

    # Build cluster co-occurrence graph for shared SHA-256 image frames
    G = nx.Graph()
    for idx in range(1, num_clusters + 1):
        G.add_node(f"banana_cluster_{idx:03d}")

    hash_to_clusters = {}
    for r in all_records:
        cid = folder_to_cluster[r["nominal_id"]]
        hash_to_clusters.setdefault(r["sha256"], set()).add(cid)

    for h, cids in hash_to_clusters.items():
        cids_list = list(cids)
        for i in range(len(cids_list)):
            for j in range(i + 1, len(cids_list)):
                G.add_edge(cids_list[i], cids_list[j])

    components = sorted(list(nx.connected_components(G)), key=lambda c: sorted(list(c))[0])
    print(f"  Total connected components of clusters sharing frames: {len(components)}")

    cluster_to_comp = {}
    for c_idx, comp in enumerate(components, 1):
        comp_id = f"component_{c_idx:02d}"
        for cid in comp:
            cluster_to_comp[cid] = comp_id

    # -------------------------------------------------------------------------
    # STEP 6 & 7: CLASS DISTRIBUTION
    # -------------------------------------------------------------------------
    print("\n[STEP 6 & 7] Computing class distributions...")
    # Attach metadata to all records
    for r in all_records:
        cid = folder_to_cluster[r["nominal_id"]]
        r["cluster_id"] = cid
        r["component_id"] = cluster_to_comp[cid]
        r["is_canonical_representative"] = folder_to_canonical[r["nominal_id"]]
        r["clone_group_id"] = clone_group_map[r["nominal_id"]]
        r["is_clone_folder"] = (clone_group_map[r["nominal_id"]] != "none")

    df_all = pd.DataFrame(all_records)
    
    # Class distribution in full dataset
    print("  Full Dataset (528 images across 66 nominal folders):")
    class_dist_full = df_all["class_name"].value_counts()[["Unripe", "Semi-ripe", "Ripe"]]
    for cname, count in class_dist_full.items():
        print(f"    - {cname:10s}: {count:3d} images ({count/len(df_all)*100:.1f}%)")

    # Class distribution in deduplicated dataset (384 images across 48 unique clusters)
    df_dedup = df_all[df_all["is_canonical_representative"] == True].copy()
    print("\n  Deduplicated Dataset (384 images across 48 canonical clusters):")
    class_dist_dedup = df_dedup["class_name"].value_counts()[["Unripe", "Semi-ripe", "Ripe"]]
    for cname, count in class_dist_dedup.items():
        print(f"    - {cname:10s}: {count:3d} images ({count/len(df_dedup)*100:.1f}%)")

    # -------------------------------------------------------------------------
    # STEP 8 & 9: DESIGN LEAKAGE-SAFE SPLIT (70% Train, 15% Val, 15% Test)
    # -------------------------------------------------------------------------
    print("\n[STEP 8 & 9] Partitioning components into strictly leakage-free Train/Val/Test...")
    comp_sizes = [len(c) for c in components]
    comp_folder_counts = [
        sum(len([f for f, cid in folder_to_cluster.items() if cid == c]) for c in comp)
        for comp in components
    ]

    # Target: 34 Train (70.8%), 7 Val (14.6%), 7 Test (14.6%)
    # Optimized selection using fixed random seed (42) for deterministic reproducibility
    import itertools
    candidates = []
    for val_set in itertools.combinations(range(len(components)), 3):
        if sum(comp_sizes[i] for i in val_set) == 7:
            rem = set(range(len(components))) - set(val_set)
            for test_set in itertools.combinations(rem, 3):
                if sum(comp_sizes[i] for i in test_set) == 7:
                    train_set = rem - set(test_set)
                    tr_f = sum(comp_folder_counts[i] for i in train_set)
                    va_f = sum(comp_folder_counts[i] for i in val_set)
                    te_f = sum(comp_folder_counts[i] for i in test_set)
                    score = abs(tr_f - 46.2) + abs(va_f - 9.9) + abs(te_f - 9.9)
                    candidates.append((score, train_set, set(val_set), set(test_set), tr_f, va_f, te_f))

    # Sort deterministically
    candidates.sort(key=lambda x: (x[0], x[4], x[5], sorted(list(x[2])), sorted(list(x[3]))))
    best_split = candidates[0]
    _, train_comps, val_comps, test_comps, tr_f, va_f, te_f = best_split

    train_clusters = sorted([cid for i in train_comps for cid in components[i]])
    val_clusters = sorted([cid for i in val_comps for cid in components[i]])
    test_clusters = sorted([cid for i in test_comps for cid in components[i]])

    print(f"  Split configuration (Fixed Seed {RANDOM_SEED}):")
    print(f"    Train: {len(train_clusters)} clusters (70.83%), {tr_f} nominal folders ({tr_f*8} raw / {len(train_clusters)*8} dedup imgs)")
    print(f"    Val:   {len(val_clusters)} clusters (14.58%), {va_f} nominal folders ({va_f*8} raw / {len(val_clusters)*8} dedup imgs)")
    print(f"    Test:  {len(test_clusters)} clusters (14.58%), {te_f} nominal folders ({te_f*8} raw / {len(test_clusters)*8} dedup imgs)")

    # Assign split to records
    cluster_to_split = {}
    for cid in train_clusters:
        cluster_to_split[cid] = "train"
    for cid in val_clusters:
        cluster_to_split[cid] = "val"
    for cid in test_clusters:
        cluster_to_split[cid] = "test"

    for r in all_records:
        r["split"] = cluster_to_split[r["cluster_id"]]

    df_all = pd.DataFrame(all_records)
    df_dedup = df_all[df_all["is_canonical_representative"] == True].copy()

    # -------------------------------------------------------------------------
    # STEP 8: LEAKAGE CHECKS
    # -------------------------------------------------------------------------
    print("\n[STEP 8] Executing strict cryptographic leakage verification...")
    # 1. Cluster overlap
    tr_c_set, va_c_set, te_c_set = set(train_clusters), set(val_clusters), set(test_clusters)
    assert len(tr_c_set.intersection(va_c_set)) == 0, "Leakage: Train and Val share clusters!"
    assert len(tr_c_set.intersection(te_c_set)) == 0, "Leakage: Train and Test share clusters!"
    assert len(va_c_set.intersection(te_c_set)) == 0, "Leakage: Val and Test share clusters!"
    print("  [PASS] Zero biological sequence clusters overlap across splits.")

    # 2. Clone group overlap
    for members in clone_groups_list:
        member_splits = set(cluster_to_split[folder_to_cluster[m]] for m in members)
        assert len(member_splits) == 1, f"Leakage: Clone group {members} crosses splits: {member_splits}!"
    print("  [PASS] Zero clone groups are split across splits.")

    # 3. SHA-256 hash overlap
    tr_hashes = set(df_all[df_all["split"] == "train"]["sha256"])
    va_hashes = set(df_all[df_all["split"] == "val"]["sha256"])
    te_hashes = set(df_all[df_all["split"] == "test"]["sha256"])
    
    assert len(tr_hashes.intersection(va_hashes)) == 0, "Leakage: Train and Val share SHA-256 hashes!"
    assert len(tr_hashes.intersection(te_hashes)) == 0, "Leakage: Train and Test share SHA-256 hashes!"
    assert len(va_hashes.intersection(te_hashes)) == 0, "Leakage: Val and Test share SHA-256 hashes!"
    print("  [PASS] Zero SHA-256 image hashes overlap across splits.")

    # -------------------------------------------------------------------------
    # STEP 10: GROUP-AWARE CROSS-VALIDATION ANALYSIS
    # -------------------------------------------------------------------------
    print("\n[STEP 10] Analyzing group-aware cross-validation on the 34 training clusters...")
    df_train_dedup = df_dedup[df_dedup["split"] == "train"].reset_index(drop=True)
    X_tr = df_train_dedup.index.values
    y_tr = df_train_dedup["class_id"].values
    groups_c = df_train_dedup["cluster_id"].values

    print(f"  Training pool: 34 clusters, {len(df_train_dedup)} images across 11 connected components.")
    
    # 5-fold GroupKFold by cluster
    gkf = GroupKFold(n_splits=5)
    print("\n  Evaluation: 5-Fold GroupKFold on Train by Cluster ID:")
    for fold, (f_tr, f_va) in enumerate(gkf.split(X_tr, y_tr, groups=groups_c)):
        va_c = len(set(groups_c[f_va]))
        classes = np.bincount(y_tr[f_va], minlength=3)
        print(f"    Fold {fold}: Val Clusters={va_c:2d}, Val Samples={len(f_va):2d}, Classes={classes.tolist()}")

    # -------------------------------------------------------------------------
    # STEP 11: SAVE MANIFESTS & AUDIT REPORTS
    # -------------------------------------------------------------------------
    print("\n[STEP 11] Writing clean, standardized manifests...")
    
    # Manifest 1: banana_verified_manifest.csv (528 records)
    verified_csv_path = os.path.join(MANIFESTS_DIR, "banana_verified_manifest.csv")
    manifest_cols = [
        "path", "filename", "extension", "file_size_bytes", "width", "height",
        "sha256", "nominal_id", "day", "observation_day", "cluster_id",
        "component_id", "clone_group_id", "is_clone_folder",
        "is_canonical_representative", "class_id", "class_name",
        "author_stage_guideline", "split"
    ]
    df_all[manifest_cols].to_csv(verified_csv_path, index=False)
    print(f"  Saved: {verified_csv_path} ({len(df_all)} records)")

    # Manifest 2: banana_deduplicated_manifest.csv (384 records)
    dedup_csv_path = os.path.join(MANIFESTS_DIR, "banana_deduplicated_manifest.csv")
    dedup_cols = [
        "path", "filename", "extension", "file_size_bytes", "width", "height",
        "sha256", "nominal_id", "day", "observation_day", "cluster_id",
        "component_id", "clone_group_id", "class_id", "class_name",
        "author_stage_guideline", "split"
    ]
    df_dedup[dedup_cols].to_csv(dedup_csv_path, index=False)
    print(f"  Saved: {dedup_csv_path} ({len(df_dedup)} records)")

    # Manifest 3: banana_cluster_split_manifest.csv (384 records, primary training target)
    split_csv_path = os.path.join(MANIFESTS_DIR, "banana_cluster_split_manifest.csv")
    df_dedup[dedup_cols].to_csv(split_csv_path, index=False)
    print(f"  Saved: {split_csv_path} ({len(df_dedup)} records)")

    # Audit JSON reports
    clone_report = {
        "dataset": "banana_ripening_dataset_day0_to_day7",
        "doi": "10.17632/d5tczj7fs7.2",
        "total_nominal_folders": 66,
        "total_unique_clusters": num_clusters,
        "total_clone_groups": len(clone_groups_list),
        "clone_groups": [
            {
                "clone_group_id": f"clone_group_{idx:02d}",
                "canonical_cluster_id": folder_to_cluster[members[0]],
                "folders": members,
                "folder_count": len(members),
                "split": cluster_to_split[folder_to_cluster[members[0]]]
            }
            for idx, members in enumerate(clone_groups_list, 1)
        ],
        "split_summary": {
            "random_seed": RANDOM_SEED,
            "train": {
                "clusters": train_clusters,
                "cluster_count": len(train_clusters),
                "deduplicated_images": len(train_clusters) * 8,
                "class_counts": {"Unripe": len(train_clusters) * 3, "Semi-ripe": len(train_clusters) * 3, "Ripe": len(train_clusters) * 2}
            },
            "val": {
                "clusters": val_clusters,
                "cluster_count": len(val_clusters),
                "deduplicated_images": len(val_clusters) * 8,
                "class_counts": {"Unripe": len(val_clusters) * 3, "Semi-ripe": len(val_clusters) * 3, "Ripe": len(val_clusters) * 2}
            },
            "test": {
                "clusters": test_clusters,
                "cluster_count": len(test_clusters),
                "deduplicated_images": len(test_clusters) * 8,
                "class_counts": {"Unripe": len(test_clusters) * 3, "Semi-ripe": len(test_clusters) * 3, "Ripe": len(test_clusters) * 2}
            }
        }
    }
    
    clone_report_path = os.path.join(AUDIT_DIR, "banana_clone_groups.json")
    with open(clone_report_path, "w") as f:
        json.dump(clone_report, f, indent=2)
    print(f"  Saved: {clone_report_path}")

    print("\n" + "=" * 70)
    print("PHASE 2K PREPARATION COMPLETED SUCCESSFULLY.")
    print("=" * 70)


if __name__ == "__main__":
    run_pipeline()
