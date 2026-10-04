import json
from pathlib import Path
from typing import Dict, Tuple

import numpy as np
import pandas as pd
import torch
from torch.utils.data import DataLoader

from ml.config import TrainingConfig
from ml.dataset import AvocadoDataset, verify_and_load_splits, get_eval_transforms
from ml.model import build_mobilenet_v3_small


def extract_features_and_targets(
    config: TrainingConfig,
    cache_path: Path = Path("ml/saved_models/shelflife_extracted_features.csv"),
    force_recompute: bool = False
) -> pd.DataFrame:
    """
    Extracts visual features from the frozen Phase 1A MobileNetV3-Small checkpoint
    and calculates ground-truth Remaining Usable Life (RUL) targets.
    
    Target:
    RUL = max(0, D_stage5 - d)
    
    Features:
    - p1, p2, p3, p4, p5 (softmax probabilities from MobileNetV3)
    - expected_stage (sum_k k * p_k)
    - confidence (max_k p_k)
    - predicted_stage (argmax_k p_k + 1)
    - temperature_c (10.0 for T10, 20.0 for T20 and Tam)
    - is_cold_storage (1 for T10, 0 for T20/Tam)
    
    Ground-truth ripening_stage is kept for analysis/stratification ONLY,
    NEVER as an inference feature for RUL models.
    """
    if cache_path.exists() and not force_recompute:
        print(f"Loading cached features from {cache_path.resolve()}...")
        return pd.read_csv(cache_path)

    config.set_seed()
    device = config.device
    checkpoint_path = config.get_best_model_path()

    print("=" * 60)
    print("EXTRACTING PHASE 1B SHELF-LIFE FEATURES USING FROZEN CV MODEL")
    print(f"Checkpoint: {checkpoint_path.resolve()}")
    print("=" * 60)

    checkpoint = torch.load(checkpoint_path, map_location=device)
    model = build_mobilenet_v3_small(
        num_classes=config.num_classes,
        pretrained=False,
        freeze_backbone=False
    )
    model.load_state_dict(checkpoint["model_state_dict"])
    model = model.to(device)
    model.eval()

    # 1. Load verified manifests
    master_df = pd.read_csv(config.manifest_dir / "master_manifest.csv")
    train_df, val_df, test_df = verify_and_load_splits(config)

    # 2. Identify Stage 5 transition day per specimen
    stage5_day_map = master_df[master_df["ripening_stage"] == 5].groupby("specimen_id")["day"].min().to_dict()

    combined_splits = [
        ("train", train_df),
        ("val", val_df),
        ("test", test_df)
    ]

    eval_transform = get_eval_transforms(config.image_size, config.imagenet_mean, config.imagenet_std)
    extracted_rows = []

    softmax = torch.nn.Softmax(dim=1)

    for split_name, df_split in combined_splits:
        print(f"Extracting features for {split_name} split ({len(df_split):,} images)...")
        ds = AvocadoDataset(df_split, config.data_root, eval_transform)
        loader = DataLoader(ds, batch_size=config.batch_size, shuffle=False, num_workers=0)

        batch_start = 0
        with torch.no_grad():
            for images, labels, specimen_ids in loader:
                images = images.to(device)
                logits = model(images)
                probs = softmax(logits).cpu().numpy()

                batch_size = images.size(0)
                sub_df = df_split.iloc[batch_start : batch_start + batch_size]

                for i in range(batch_size):
                    row = sub_df.iloc[i]
                    p = probs[i]
                    exp_stage = float(np.sum(p * np.arange(1, 6)))
                    conf = float(np.max(p))
                    pred_stage = int(np.argmax(p) + 1)

                    spec_id = str(row["specimen_id"])
                    sg = str(row["storage_group"])
                    obs_day = int(row["day"])
                    actual_stage = int(row["ripening_stage"])

                    # Temperature assignment based on verified experimental storage conditions
                    if sg == "T10":
                        temp_c = 10.0
                        is_cold = 1
                    else:  # T20 or Tam (~20°C - 22°C ambient)
                        temp_c = 20.0
                        is_cold = 0

                    # Ground truth Stage 5 terminal day
                    if spec_id in stage5_day_map:
                        d_s5 = stage5_day_map[spec_id]
                        actual_rul = float(max(0, d_s5 - obs_day))
                        is_censored = False
                    else:
                        d_s5 = np.nan
                        actual_rul = np.nan
                        is_censored = True

                    extracted_rows.append({
                        "filename": row["filename"],
                        "specimen_id": spec_id,
                        "split": split_name,
                        "storage_group": sg,
                        "temperature_c": temp_c,
                        "is_cold_storage": is_cold,
                        "observation_day": obs_day,
                        "actual_stage": actual_stage,
                        "terminal_stage5_day": d_s5,
                        "is_censored": is_censored,
                        "actual_rul": actual_rul,
                        # Model extracted features
                        "p1": float(p[0]),
                        "p2": float(p[1]),
                        "p3": float(p[2]),
                        "p4": float(p[3]),
                        "p5": float(p[4]),
                        "expected_stage": exp_stage,
                        "confidence": conf,
                        "predicted_stage": pred_stage
                    })

                batch_start += batch_size

    feature_df = pd.DataFrame(extracted_rows)
    cache_path.parent.mkdir(parents=True, exist_ok=True)
    feature_df.to_csv(cache_path, index=False)
    print(f"\nSaved extracted features dataframe to {cache_path.resolve()} ({len(feature_df):,} rows)")
    return feature_df
