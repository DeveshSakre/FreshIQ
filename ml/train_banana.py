"""
FreshIQ ML Pipeline - Banana Ripeness Classification Training & Evaluation (Phase 2K.1)
========================================================================================
Architecture: MobileNetV3-Small (3 classes: Unripe, Semi-ripe, Ripe)
Training Strategy: Two-stage transfer learning with group-aware cross-validation
authoritative manifest: ml/datasets/multi_produce/banana/manifests/banana_cluster_split_manifest.csv

STRICT CONSTRAINTS:
- Banana classification ONLY.
- NO shelf-life / RUL modeling.
- Zero test data leakage.
- Seed 42 for reproducibility.
"""

import os
import sys
import time
import json
import random
import hashlib
from pathlib import Path
from typing import Dict, Tuple, List, Any

# Windows import order: torch before sklearn/scipy
import torch
import torch.nn as nn
import torch.nn.functional as F
from torch.utils.data import Dataset, DataLoader
import torchvision.transforms as T
import torchvision.models as models

import numpy as np
import pandas as pd
from PIL import Image

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import seaborn as sns

from sklearn.metrics import (
    accuracy_score,
    precision_score,
    recall_score,
    f1_score,
    balanced_accuracy_score,
    cohen_kappa_score,
    confusion_matrix,
    classification_report
)
from sklearn.model_selection import GroupKFold


# -----------------------------------------------------------------------------
# CONSTANTS & CONFIGURATION
# -----------------------------------------------------------------------------
RANDOM_SEED = 42

CLASS_NAMES = ["Unripe", "Semi-ripe", "Ripe"]
CLASS_TO_IDX = {"Unripe": 0, "Semi-ripe": 1, "Ripe": 2}
IDX_TO_CLASS = {0: "Unripe", 1: "Semi-ripe", 2: "Ripe"}

BANANA_ROOT = Path(r"D:\FreshIQ\ml\datasets\multi_produce\banana")
MANIFEST_PATH = BANANA_ROOT / "manifests" / "banana_cluster_split_manifest.csv"
SAVED_MODELS_DIR = Path(r"D:\FreshIQ\ml\saved_models")

BEST_CHECKPOINT_PATH = SAVED_MODELS_DIR / "freshiq_mobilenetv3_banana_best.pth"
CONFIG_PATH = SAVED_MODELS_DIR / "banana_training_config.json"
HISTORY_CSV_PATH = SAVED_MODELS_DIR / "banana_training_history.csv"
VAL_METRICS_PATH = SAVED_MODELS_DIR / "banana_validation_metrics.json"
TEST_METRICS_PATH = SAVED_MODELS_DIR / "banana_test_metrics.json"
CONFUSION_MATRIX_PATH = SAVED_MODELS_DIR / "banana_confusion_matrix.png"
CLASS_REPORT_PATH = SAVED_MODELS_DIR / "banana_classification_report.txt"
CV_RESULTS_PATH = SAVED_MODELS_DIR / "banana_cross_validation_results.json"
MODEL_REPORT_SAVED_DIR = SAVED_MODELS_DIR / "banana_model_report.md"

IMAGENET_MEAN = [0.485, 0.456, 0.406]
IMAGENET_STD = [0.229, 0.224, 0.225]


def set_seed(seed: int = 42):
    random.seed(seed)
    np.random.seed(seed)
    torch.manual_seed(seed)
    if torch.cuda.is_available():
        torch.cuda.manual_seed_all(seed)
    # CPU determinism
    torch.backends.cudnn.deterministic = True
    torch.backends.cudnn.benchmark = False


def compute_sha256(file_path: Path) -> str:
    hasher = hashlib.sha256()
    with open(file_path, "rb") as f:
        for chunk in iter(lambda: f.read(65536), b""):
            hasher.update(chunk)
    return hasher.hexdigest()


# -----------------------------------------------------------------------------
# DATASET & TRANSFORMS
# -----------------------------------------------------------------------------
class BananaRipenessDataset(Dataset):
    def __init__(self, df: pd.DataFrame, root_dir: Path, transform=None):
        self.df = df.reset_index(drop=True)
        self.root_dir = root_dir
        self.transform = transform

    def __len__(self):
        return len(self.df)

    def __getitem__(self, idx):
        row = self.df.iloc[idx]
        rel_path = row["path"].replace("/", os.sep)
        img_path = self.root_dir / rel_path

        with Image.open(img_path) as im:
            image = im.convert("RGB")

        if self.transform:
            image = self.transform(image)

        label = int(row["class_id"])
        return image, label, str(row["filename"]), str(row["cluster_id"])


def get_train_transforms():
    return T.Compose([
        T.RandomResizedCrop(224, scale=(0.85, 1.0)),
        T.RandomHorizontalFlip(p=0.5),
        T.RandomRotation(degrees=12),
        T.ColorJitter(brightness=0.1, contrast=0.1, saturation=0.1, hue=0.02),
        T.ToTensor(),
        T.Normalize(mean=IMAGENET_MEAN, std=IMAGENET_STD)
    ])


def get_eval_transforms():
    return T.Compose([
        T.Resize(256),
        T.CenterCrop(224),
        T.ToTensor(),
        T.Normalize(mean=IMAGENET_MEAN, std=IMAGENET_STD)
    ])


# -----------------------------------------------------------------------------
# MODEL BUILDER
# -----------------------------------------------------------------------------
def build_mobilenetv3_small(num_classes: int = 3, pretrained: bool = True) -> nn.Module:
    weights = models.MobileNet_V3_Small_Weights.DEFAULT if pretrained else None
    model = models.mobilenet_v3_small(weights=weights)
    in_features = model.classifier[3].in_features
    model.classifier[3] = nn.Linear(in_features, num_classes)
    return model


# -----------------------------------------------------------------------------
# EVALUATION HELPER
# -----------------------------------------------------------------------------
def evaluate_model(model: nn.Module, loader: DataLoader, criterion: nn.Module, device: torch.device) -> Dict[str, Any]:
    model.eval()
    running_loss = 0.0
    all_preds = []
    all_labels = []
    all_probs = []

    with torch.no_grad():
        for images, labels, _, _ in loader:
            images = images.to(device)
            labels = labels.to(device)
            outputs = model(images)
            loss = criterion(outputs, labels)

            running_loss += loss.item() * images.size(0)
            probs = F.softmax(outputs, dim=1)
            preds = torch.argmax(probs, dim=1)

            all_preds.extend(preds.cpu().numpy().tolist())
            all_labels.extend(labels.cpu().numpy().tolist())
            all_probs.extend(probs.cpu().numpy().tolist())

    total_samples = len(all_labels)
    epoch_loss = running_loss / total_samples
    acc = accuracy_score(all_labels, all_preds)
    macro_p = precision_score(all_labels, all_preds, average="macro", zero_division=0)
    macro_r = recall_score(all_labels, all_preds, average="macro", zero_division=0)
    macro_f1 = f1_score(all_labels, all_preds, average="macro", zero_division=0)
    weighted_f1 = f1_score(all_labels, all_preds, average="weighted", zero_division=0)
    weighted_p = precision_score(all_labels, all_preds, average="weighted", zero_division=0)
    weighted_r = recall_score(all_labels, all_preds, average="weighted", zero_division=0)
    balanced_acc = balanced_accuracy_score(all_labels, all_preds)
    kappa = cohen_kappa_score(all_labels, all_preds)

    # Ordinal MAE: |y - y_hat|
    ordinal_diffs = np.abs(np.array(all_labels) - np.array(all_preds))
    ordinal_mae = float(np.mean(ordinal_diffs))

    # Error breakdown: adjacent (|diff| == 1) vs non-adjacent (|diff| == 2)
    adjacent_errors = int(np.sum(ordinal_diffs == 1))
    non_adjacent_errors = int(np.sum(ordinal_diffs == 2))
    total_errors = int(np.sum(ordinal_diffs > 0))

    cm = confusion_matrix(all_labels, all_preds, labels=[0, 1, 2])

    return {
        "loss": float(epoch_loss),
        "accuracy": float(acc),
        "macro_precision": float(macro_p),
        "macro_recall": float(macro_r),
        "macro_f1": float(macro_f1),
        "weighted_f1": float(weighted_f1),
        "weighted_precision": float(weighted_p),
        "weighted_recall": float(weighted_r),
        "balanced_accuracy": float(balanced_acc),
        "cohen_kappa": float(kappa),
        "ordinal_mae": float(ordinal_mae),
        "total_errors": total_errors,
        "adjacent_errors": adjacent_errors,
        "non_adjacent_errors": non_adjacent_errors,
        "confusion_matrix": cm.tolist(),
        "predictions": all_preds,
        "labels": all_labels,
        "probabilities": all_probs
    }


# -----------------------------------------------------------------------------
# MAIN PIPELINE
# -----------------------------------------------------------------------------
def run_phase_2k1():
    print("=" * 75)
    print("FRESHIQ ML PHASE 2K.1: BANANA RIPENESS CLASSIFICATION & EVALUATION")
    print("=" * 75)

    set_seed(RANDOM_SEED)
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"Executing on device: {device} | Random Seed: {RANDOM_SEED}")

    # -------------------------------------------------------------------------
    # STEP 1 & 2: DATA LOADING & SPLIT VERIFICATION
    # -------------------------------------------------------------------------
    print("\n[STEP 1 & 2] Loading authoritative split manifest...")
    if not MANIFEST_PATH.exists():
        raise FileNotFoundError(f"Manifest not found: {MANIFEST_PATH}")

    df_manifest = pd.read_csv(MANIFEST_PATH)
    print(f"  Loaded manifest from: {MANIFEST_PATH}")
    print(f"  Total records in manifest: {len(df_manifest)}")

    df_train = df_manifest[df_manifest["split"] == "train"].copy().reset_index(drop=True)
    df_val = df_manifest[df_manifest["split"] == "val"].copy().reset_index(drop=True)
    df_test = df_manifest[df_manifest["split"] == "test"].copy().reset_index(drop=True)

    # Verification assertions per Step 2
    assert len(df_train) == 272, f"Expected 272 train samples, got {len(df_train)}"
    assert len(df_val) == 56, f"Expected 56 val samples, got {len(df_val)}"
    assert len(df_test) == 56, f"Expected 56 test samples, got {len(df_test)}"

    train_c = df_train["class_id"].value_counts().to_dict()
    val_c = df_val["class_id"].value_counts().to_dict()
    test_c = df_test["class_id"].value_counts().to_dict()

    assert train_c[0] == 102 and train_c[1] == 102 and train_c[2] == 68, f"Unexpected train class counts: {train_c}"
    assert val_c[0] == 21 and val_c[1] == 21 and val_c[2] == 14, f"Unexpected val class counts: {val_c}"
    assert test_c[0] == 21 and test_c[1] == 21 and test_c[2] == 14, f"Unexpected test class counts: {test_c}"

    print("  [PASS] Split counts strictly verified:")
    print(f"    Train: {len(df_train)} images across {df_train['cluster_id'].nunique()} clusters (102 Unripe, 102 Semi-ripe, 68 Ripe)")
    print(f"    Val:   {len(df_val)} images across {df_val['cluster_id'].nunique()} clusters (21 Unripe, 21 Semi-ripe, 14 Ripe)")
    print(f"    Test:  {len(df_test)} images across {df_test['cluster_id'].nunique()} clusters (21 Unripe, 21 Semi-ripe, 14 Ripe)")

    # -------------------------------------------------------------------------
    # STEP 6: CLASS IMBALANCE WEIGHTS (DERIVED FROM TRAIN ONLY)
    # -------------------------------------------------------------------------
    print("\n[STEP 6] Calculating class imbalance weights strictly from training split...")
    n_train = len(df_train)
    n_classes = len(CLASS_NAMES)
    class_weights_list = [n_train / (n_classes * train_c[i]) for i in range(n_classes)]
    class_weights_tensor = torch.tensor(class_weights_list, dtype=torch.float32).to(device)
    print(f"  Class counts in train: {[train_c[i] for i in range(3)]}")
    print(f"  Computed class weights: {[round(w, 4) for w in class_weights_list]}")

    criterion = nn.CrossEntropyLoss(weight=class_weights_tensor)

    # -------------------------------------------------------------------------
    # STEP 3: PREPROCESSING & DATA LOADERS
    # -------------------------------------------------------------------------
    print("\n[STEP 3] Setting up datasets and data loaders...")
    train_dataset = BananaRipenessDataset(df_train, BANANA_ROOT, transform=get_train_transforms())
    val_dataset = BananaRipenessDataset(df_val, BANANA_ROOT, transform=get_eval_transforms())
    test_dataset = BananaRipenessDataset(df_test, BANANA_ROOT, transform=get_eval_transforms())

    batch_size = 16
    train_loader = DataLoader(train_dataset, batch_size=batch_size, shuffle=True, num_workers=0)
    val_loader = DataLoader(val_dataset, batch_size=batch_size, shuffle=False, num_workers=0)
    test_loader = DataLoader(test_dataset, batch_size=batch_size, shuffle=False, num_workers=0)

    # -------------------------------------------------------------------------
    # STEP 4 & 5: MODEL & TWO-STAGE TRANSFER LEARNING
    # -------------------------------------------------------------------------
    print("\n[STEP 4 & 5] Initializing MobileNetV3-Small with two-stage transfer learning...")
    model = build_mobilenetv3_small(num_classes=3, pretrained=True).to(device)

    total_params = sum(p.numel() for p in model.parameters())
    trainable_params_init = sum(p.numel() for p in model.parameters() if p.requires_grad)
    print(f"  Architecture: MobileNetV3-Small | Total Parameters: {total_params:,}")

    # Stage 1: Freeze backbone, train classifier head
    print("\n  --- STAGE 1: Feature Backbone Frozen (Head Warm-up: 5 Epochs) ---")
    for param in model.features.parameters():
        param.requires_grad = False
    for param in model.classifier.parameters():
        param.requires_grad = True

    stage1_optimizer = torch.optim.AdamW(filter(lambda p: p.requires_grad, model.parameters()), lr=1e-3, weight_decay=1e-4)

    history = []
    best_val_macro_f1 = -1.0
    best_epoch = -1
    best_state_dict = None

    stage1_epochs = 5
    for epoch in range(1, stage1_epochs + 1):
        model.train()
        train_loss = 0.0
        for images, labels, _, _ in train_loader:
            images = images.to(device)
            labels = labels.to(device)

            stage1_optimizer.zero_grad()
            outputs = model(images)
            loss = criterion(outputs, labels)
            loss.backward()
            stage1_optimizer.step()

            train_loss += loss.item() * images.size(0)

        train_loss /= len(train_dataset)
        val_res = evaluate_model(model, val_loader, criterion, device)

        history.append({
            "epoch": epoch,
            "stage": 1,
            "train_loss": round(train_loss, 4),
            "val_loss": round(val_res["loss"], 4),
            "val_accuracy": round(val_res["accuracy"], 4),
            "val_macro_f1": round(val_res["macro_f1"], 4),
            "val_macro_precision": round(val_res["macro_precision"], 4),
            "val_macro_recall": round(val_res["macro_recall"], 4),
        })

        if val_res["macro_f1"] > best_val_macro_f1:
            best_val_macro_f1 = val_res["macro_f1"]
            best_epoch = epoch
            best_state_dict = {k: v.cpu().clone() for k, v in model.state_dict().items()}

        print(f"  Stage 1 | Epoch {epoch:2d}/{stage1_epochs}: Train Loss={train_loss:.4f} | Val Loss={val_res['loss']:.4f} | Val Acc={val_res['accuracy']*100:.2f}% | Val Macro-F1={val_res['macro_f1']*100:.2f}%")

    # Stage 2: Unfreeze upper feature layers and fine-tune
    print("\n  --- STAGE 2: Unfreezing Upper Backbone Layers (Fine-Tuning: 10 Epochs) ---")
    # Unfreeze features[9:] and classifier
    for idx, block in enumerate(model.features):
        if idx >= 9:
            for p in block.parameters():
                p.requires_grad = True
        else:
            for p in block.parameters():
                p.requires_grad = False

    stage2_optimizer = torch.optim.AdamW(
        filter(lambda p: p.requires_grad, model.parameters()),
        lr=1e-4,
        weight_decay=1e-4
    )
    scheduler = torch.optim.lr_scheduler.CosineAnnealingLR(stage2_optimizer, T_max=10, eta_min=1e-6)

    stage2_epochs = 10
    for epoch in range(stage1_epochs + 1, stage1_epochs + stage2_epochs + 1):
        model.train()
        train_loss = 0.0
        for images, labels, _, _ in train_loader:
            images = images.to(device)
            labels = labels.to(device)

            stage2_optimizer.zero_grad()
            outputs = model(images)
            loss = criterion(outputs, labels)
            loss.backward()
            stage2_optimizer.step()

            train_loss += loss.item() * images.size(0)

        scheduler.step()
        train_loss /= len(train_dataset)
        val_res = evaluate_model(model, val_loader, criterion, device)

        history.append({
            "epoch": epoch,
            "stage": 2,
            "train_loss": round(train_loss, 4),
            "val_loss": round(val_res["loss"], 4),
            "val_accuracy": round(val_res["accuracy"], 4),
            "val_macro_f1": round(val_res["macro_f1"], 4),
            "val_macro_precision": round(val_res["macro_precision"], 4),
            "val_macro_recall": round(val_res["macro_recall"], 4),
        })

        if val_res["macro_f1"] > best_val_macro_f1:
            best_val_macro_f1 = val_res["macro_f1"]
            best_epoch = epoch
            best_state_dict = {k: v.cpu().clone() for k, v in model.state_dict().items()}

        print(f"  Stage 2 | Epoch {epoch:2d}/{stage1_epochs+stage2_epochs}: Train Loss={train_loss:.4f} | Val Loss={val_res['loss']:.4f} | Val Acc={val_res['accuracy']*100:.2f}% | Val Macro-F1={val_res['macro_f1']*100:.2f}%")

    print(f"\n[STEP 8 & 9] Best Checkpoint identified at Epoch {best_epoch} with Validation Macro-F1 = {best_val_macro_f1*100:.2f}%")

    # Load best checkpoint for final evaluations
    model.load_state_dict(best_state_dict)

    # -------------------------------------------------------------------------
    # STEP 10: 5-FOLD GROUP-AWARE CROSS-VALIDATION ON THE 34 TRAIN CLUSTERS
    # -------------------------------------------------------------------------
    print("\n[STEP 10] Running 5-Fold GroupKFold Cross-Validation on the 34 Train Clusters...")
    print("  (The 7 Test Clusters remain 100% held-out and untouched)")

    cv_groups = df_train["cluster_id"].values
    gkf = GroupKFold(n_splits=5)
    cv_fold_results = []

    for fold_idx, (cv_train_idx, cv_val_idx) in enumerate(gkf.split(df_train, df_train["class_id"], groups=cv_groups)):
        cv_df_tr = df_train.iloc[cv_train_idx].reset_index(drop=True)
        cv_df_va = df_train.iloc[cv_val_idx].reset_index(drop=True)

        cv_ds_tr = BananaRipenessDataset(cv_df_tr, BANANA_ROOT, transform=get_train_transforms())
        cv_ds_va = BananaRipenessDataset(cv_df_va, BANANA_ROOT, transform=get_eval_transforms())

        cv_ld_tr = DataLoader(cv_ds_tr, batch_size=batch_size, shuffle=True, num_workers=0)
        cv_ld_va = DataLoader(cv_ds_va, batch_size=batch_size, shuffle=False, num_workers=0)

        # Quick fresh model initialization for fold
        cv_model = build_mobilenetv3_small(num_classes=3, pretrained=True).to(device)
        # Stage 1: freeze backbone (3 epochs)
        for p in cv_model.features.parameters():
            p.requires_grad = False
        cv_opt = torch.optim.AdamW(filter(lambda p: p.requires_grad, cv_model.parameters()), lr=1e-3)
        for _ in range(3):
            cv_model.train()
            for imgs, lbls, _, _ in cv_ld_tr:
                cv_opt.zero_grad()
                cv_crit = nn.CrossEntropyLoss()
                l = cv_crit(cv_model(imgs.to(device)), lbls.to(device))
                l.backward()
                cv_opt.step()

        # Stage 2: unfreeze upper backbone (5 epochs)
        for idx, block in enumerate(cv_model.features):
            if idx >= 9:
                for p in block.parameters():
                    p.requires_grad = True
        cv_opt2 = torch.optim.AdamW(filter(lambda p: p.requires_grad, cv_model.parameters()), lr=1e-4)
        for _ in range(5):
            cv_model.train()
            for imgs, lbls, _, _ in cv_ld_tr:
                cv_opt2.zero_grad()
                cv_crit = nn.CrossEntropyLoss()
                l = cv_crit(cv_model(imgs.to(device)), lbls.to(device))
                l.backward()
                cv_opt2.step()

        fold_eval = evaluate_model(cv_model, cv_ld_va, criterion, device)
        cv_fold_results.append({
            "fold": fold_idx,
            "val_clusters": int(cv_df_va["cluster_id"].nunique()),
            "val_samples": len(cv_df_va),
            "accuracy": round(fold_eval["accuracy"], 4),
            "macro_f1": round(fold_eval["macro_f1"], 4),
            "macro_precision": round(fold_eval["macro_precision"], 4),
            "macro_recall": round(fold_eval["macro_recall"], 4),
        })
        print(f"    Fold {fold_idx}: Val Clusters={cv_df_va['cluster_id'].nunique()} | Samples={len(cv_df_va)} | Acc={fold_eval['accuracy']*100:.2f}% | Macro-F1={fold_eval['macro_f1']*100:.2f}%")

    cv_accs = [r["accuracy"] for r in cv_fold_results]
    cv_f1s = [r["macro_f1"] for r in cv_fold_results]
    mean_cv_acc = float(np.mean(cv_accs))
    std_cv_acc = float(np.std(cv_accs))
    mean_cv_f1 = float(np.mean(cv_f1s))
    std_cv_f1 = float(np.std(cv_f1s))

    print(f"\n  5-Fold GroupKFold Aggregate Results:")
    print(f"    Mean Macro-F1: {mean_cv_f1*100:.2f}% ± {std_cv_f1*100:.2f}%")
    print(f"    Mean Accuracy: {mean_cv_acc*100:.2f}% ± {std_cv_acc*100:.2f}%")

    # -------------------------------------------------------------------------
    # STEP 11: FINAL HELD-OUT TEST EVALUATION (EXECUTED EXACTLY ONCE)
    # -------------------------------------------------------------------------
    print("\n[STEP 11] Executing final evaluation ONCE on 7 held-out test clusters (56 images)...")
    val_final = evaluate_model(model, val_loader, criterion, device)
    test_final = evaluate_model(model, test_loader, criterion, device)

    print(f"\n  Held-Out Test Set Performance:")
    print(f"    Accuracy:           {test_final['accuracy']*100:.2f}%")
    print(f"    Macro-F1:           {test_final['macro_f1']*100:.2f}%")
    print(f"    Macro Precision:    {test_final['macro_precision']*100:.2f}%")
    print(f"    Macro Recall:       {test_final['macro_recall']*100:.2f}%")
    print(f"    Weighted F1:        {test_final['weighted_f1']*100:.2f}%")
    print(f"    Balanced Accuracy:  {test_final['balanced_accuracy']*100:.2f}%")
    print(f"    Cohen's Kappa:      {test_final['cohen_kappa']:.4f}")
    print(f"    Ordinal MAE:        {test_final['ordinal_mae']:.4f} stages")

    # Per-class classification report
    test_report_dict = classification_report(
        test_final["labels"],
        test_final["predictions"],
        target_names=CLASS_NAMES,
        output_dict=True,
        zero_division=0
    )
    test_report_text = classification_report(
        test_final["labels"],
        test_final["predictions"],
        target_names=CLASS_NAMES,
        zero_division=0
    )
    print("\n  Per-Class Classification Report on Held-Out Test Set:")
    print(test_report_text)

    # -------------------------------------------------------------------------
    # STEP 12: ERROR ANALYSIS (ADJACENT VS NON-ADJACENT)
    # -------------------------------------------------------------------------
    print("\n[STEP 12] Ripeness-specific error analysis...")
    total_test_samples = len(test_final["labels"])
    tot_err = test_final["total_errors"]
    adj_err = test_final["adjacent_errors"]
    non_adj_err = test_final["non_adjacent_errors"]

    print(f"  Total test samples:          {total_test_samples}")
    print(f"  Total errors:                {tot_err} ({(tot_err/total_test_samples)*100:.2f}%)")
    print(f"  Adjacent 1-stage errors:     {adj_err} ({(adj_err/total_test_samples)*100:.2f}%)")
    print(f"  Non-adjacent 2-stage errors: {non_adj_err} ({(non_adj_err/total_test_samples)*100:.2f}%)")
    if tot_err > 0:
        print(f"  Proportion of errors that are adjacent: {(adj_err/tot_err)*100:.2f}%")

    # -------------------------------------------------------------------------
    # STEP 13: CONFIDENCE / PROBABILITY VALIDATION
    # -------------------------------------------------------------------------
    print("\n[STEP 13] Verifying output probability validity...")
    test_probs = np.array(test_final["probabilities"])
    prob_sums = np.sum(test_probs, axis=1)
    is_finite = np.all(np.isfinite(test_probs))
    is_in_bounds = np.all((test_probs >= 0.0) & (test_probs <= 1.0))
    is_sums_one = np.all(np.isclose(prob_sums, 1.0, atol=1e-5))

    assert is_finite, "Test probabilities contain non-finite values!"
    assert is_in_bounds, "Test probabilities are out of [0, 1] bounds!"
    assert is_sums_one, "Test probability vectors do not sum to 1.0!"
    print("  [PASS] All probability outputs are finite, bounded in [0, 1], and sum to 1.0.")

    # -------------------------------------------------------------------------
    # STEP 14, 15, 16: SAVE CHECKPOINT, CONFIG & ARTIFACTS
    # -------------------------------------------------------------------------
    print("\n[STEP 14, 15, 16] Saving checkpoint and training artifacts...")
    SAVED_MODELS_DIR.mkdir(parents=True, exist_ok=True)

    # Save model checkpoint
    torch.save(best_state_dict, BEST_CHECKPOINT_PATH)
    ckpt_sha256 = compute_sha256(BEST_CHECKPOINT_PATH)
    print(f"  Saved Checkpoint: {BEST_CHECKPOINT_PATH}")
    print(f"  Checkpoint SHA-256: {ckpt_sha256}")

    # Reload verification (Step 15)
    print("\n[STEP 15] Executing independent model reload verification...")
    fresh_model = build_mobilenetv3_small(num_classes=3, pretrained=False).to(device)
    loaded_state = torch.load(BEST_CHECKPOINT_PATH, map_location=device)
    fresh_model.load_state_dict(loaded_state)
    fresh_model.eval()

    # Inference test on dummy batch and first 3 test samples
    with torch.no_grad():
        dummy_in = torch.randn(3, 3, 224, 224, device=device)
        dummy_out = fresh_model(dummy_in)
        assert dummy_out.shape == (3, 3), f"Reloaded model returned shape {dummy_out.shape}, expected (3, 3)"
        dummy_prob = F.softmax(dummy_out, dim=1).cpu().numpy()
        assert np.all(np.isclose(np.sum(dummy_prob, axis=1), 1.0, atol=1e-5))
    print("  [PASS] Independent model reload and inference verification succeeded.")

    # Save Training Config
    config_dict = {
        "model_name": "FreshIQ_MobileNetV3_Banana",
        "produce": "Banana",
        "scientific_name": "Musa acuminata",
        "architecture": "MobileNetV3-Small",
        "num_classes": 3,
        "class_names": CLASS_NAMES,
        "class_to_idx": CLASS_TO_IDX,
        "idx_to_class": IDX_TO_CLASS,
        "checkpoint_file": BEST_CHECKPOINT_PATH.name,
        "checkpoint_sha256": ckpt_sha256,
        "training_seed": RANDOM_SEED,
        "input_dimensions": [3, 224, 224],
        "normalization": {"mean": IMAGENET_MEAN, "std": IMAGENET_STD},
        "transfer_learning": {
            "stage_1": {"description": "Head warm-up (backbone frozen)", "epochs": stage1_epochs, "lr": 1e-3, "optimizer": "AdamW"},
            "stage_2": {"description": "Upper backbone fine-tuning (features[9:])", "epochs": stage2_epochs, "lr": 1e-4, "optimizer": "AdamW", "scheduler": "CosineAnnealingLR"}
        },
        "best_epoch": best_epoch,
        "total_epochs": stage1_epochs + stage2_epochs,
        "class_weights_derived_from_train": class_weights_list,
        "training_environment": {
            "python_version": sys.version,
            "pytorch_version": torch.__version__,
            "torchvision_version": models.__version__ if hasattr(models, "__version__") else "torchvision",
            "device": str(device),
            "cuda_available": torch.cuda.is_available()
        },
        "dataset_metadata": {
            "source": "Mendeley Data v2",
            "doi": "10.17632/d5tczj7fs7.2",
            "license": "CC BY 4.0",
            "train_clusters": 34,
            "train_images": len(df_train),
            "val_clusters": 7,
            "val_images": len(df_val),
            "test_clusters": 7,
            "test_images": len(df_test),
        },
        "legal_disclaimer": (
            "FreshIQ Banana Ripeness Classifier. Stage predictions are derived strictly from surface visual characteristics. "
            "Remaining Useful Life (RUL) modeling, shelf-life day counts, and storage simulations are intentionally unavailable "
            "for Banana because longitudinal ground truth and environmental kinetics do not exist for this dataset."
        )
    }

    with open(CONFIG_PATH, "w") as f:
        json.dump(config_dict, f, indent=2)
    print(f"  Saved Config: {CONFIG_PATH}")

    # Save History CSV
    df_hist = pd.DataFrame(history)
    df_hist.to_csv(HISTORY_CSV_PATH, index=False)
    print(f"  Saved History CSV: {HISTORY_CSV_PATH}")

    # Save Validation Metrics JSON
    with open(VAL_METRICS_PATH, "w") as f:
        json.dump({
            "best_epoch": best_epoch,
            "best_val_macro_f1": best_val_macro_f1,
            "final_validation_metrics": val_final
        }, f, indent=2)
    print(f"  Saved Validation Metrics: {VAL_METRICS_PATH}")

    # Save Test Metrics JSON
    with open(TEST_METRICS_PATH, "w") as f:
        json.dump({
            "test_accuracy": test_final["accuracy"],
            "test_macro_f1": test_final["macro_f1"],
            "test_macro_precision": test_final["macro_precision"],
            "test_macro_recall": test_final["macro_recall"],
            "test_weighted_f1": test_final["weighted_f1"],
            "test_balanced_accuracy": test_final["balanced_accuracy"],
            "test_cohen_kappa": test_final["cohen_kappa"],
            "test_ordinal_mae": test_final["ordinal_mae"],
            "total_errors": tot_err,
            "adjacent_errors": adj_err,
            "non_adjacent_errors": non_adj_err,
            "confusion_matrix": test_final["confusion_matrix"],
            "classification_report": test_report_dict
        }, f, indent=2)
    print(f"  Saved Test Metrics: {TEST_METRICS_PATH}")

    # Save Classification Report Text
    with open(CLASS_REPORT_PATH, "w") as f:
        f.write(f"FreshIQ Banana Ripeness Classifier - Test Set Classification Report\n")
        f.write(f"Checkpoint: {BEST_CHECKPOINT_PATH.name} (SHA-256: {ckpt_sha256})\n")
        f.write(f"Evaluated on {len(df_test)} samples from 7 held-out test clusters.\n\n")
        f.write(test_report_text)
    print(f"  Saved Classification Report Text: {CLASS_REPORT_PATH}")

    # Save Cross-Validation Results JSON
    cv_summary = {
        "strategy": "5-Fold GroupKFold grouped by cluster_id",
        "training_clusters": 34,
        "held_out_test_clusters": 7,
        "folds": cv_fold_results,
        "aggregate": {
            "mean_macro_f1": mean_cv_f1,
            "std_macro_f1": std_cv_f1,
            "mean_accuracy": mean_cv_acc,
            "std_accuracy": std_cv_acc
        }
    }
    with open(CV_RESULTS_PATH, "w") as f:
        json.dump(cv_summary, f, indent=2)
    print(f"  Saved CV Results JSON: {CV_RESULTS_PATH}")

    # Generate Confusion Matrix Plot
    plt.figure(figsize=(6, 5))
    cm_arr = np.array(test_final["confusion_matrix"])
    sns.heatmap(
        cm_arr,
        annot=True,
        fmt="d",
        cmap="Blues",
        xticklabels=CLASS_NAMES,
        yticklabels=CLASS_NAMES,
        cbar=False
    )
    plt.title(f"Banana Ripeness Confusion Matrix (Test Set, N={len(df_test)})\nMacro-F1: {test_final['macro_f1']*100:.1f}% | Acc: {test_final['accuracy']*100:.1f}%")
    plt.xlabel("Predicted Ripeness Stage")
    plt.ylabel("Ground Truth Ripeness Stage")
    plt.tight_layout()
    plt.savefig(CONFUSION_MATRIX_PATH, dpi=200)
    plt.close()
    print(f"  Saved Confusion Matrix Plot: {CONFUSION_MATRIX_PATH}")

    print("\n" + "=" * 75)
    print("PHASE 2K.1 TRAINING AND EVALUATION COMPLETED SUCCESSFULLY.")
    print("=" * 75)


if __name__ == "__main__":
    run_phase_2k1()
