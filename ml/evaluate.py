import json
from pathlib import Path
from typing import Dict

import numpy as np
import pandas as pd
import torch
from torch.utils.data import DataLoader
from sklearn.metrics import (
    accuracy_score,
    precision_score,
    recall_score,
    f1_score,
    classification_report,
    confusion_matrix,
)

from ml.config import TrainingConfig
from ml.dataset import AvocadoDataset, verify_and_load_splits, get_eval_transforms
from ml.model import build_mobilenet_v3_small


def evaluate_test_set(config: TrainingConfig, checkpoint_path: Path = None) -> Dict:
    """
    Evaluates the saved best model checkpoint on the untouched test set.
    Generates test accuracy, macro precision, macro recall, macro F1, per-class metrics, and confusion matrix.
    """
    config.set_seed()
    device = config.device
    if checkpoint_path is None:
        checkpoint_path = config.get_best_model_path()

    if not checkpoint_path.exists():
        raise FileNotFoundError(f"Checkpoint file not found at {checkpoint_path}")

    print("=" * 60)
    print("FRESHIQ UNTOUCHED TEST SET EVALUATION")
    print(f"Loading checkpoint: {checkpoint_path.resolve()}")
    print("=" * 60)

    checkpoint = torch.load(checkpoint_path, map_location=device)

    # 1. Load test split
    _, _, test_df = verify_and_load_splits(config)

    eval_transform = get_eval_transforms(config.image_size, config.imagenet_mean, config.imagenet_std)
    test_ds = AvocadoDataset(test_df, config.data_root, eval_transform)
    test_loader = DataLoader(
        test_ds,
        batch_size=config.batch_size,
        shuffle=False,
        num_workers=config.num_workers,
        pin_memory=(device == "cuda")
    )

    # 2. Build & Load model
    model = build_mobilenet_v3_small(
        num_classes=config.num_classes,
        pretrained=False,
        freeze_backbone=False
    )
    model.load_state_dict(checkpoint["model_state_dict"])
    model = model.to(device)
    model.eval()

    y_true = []
    y_pred = []

    with torch.no_grad():
        for images, labels, _ in test_loader:
            images = images.to(device)
            logits = model(images)
            preds = logits.argmax(dim=1)

            y_true.extend(labels.numpy())
            y_pred.extend(preds.cpu().numpy())

    # 3. Calculate metrics
    acc = accuracy_score(y_true, y_pred)
    macro_prec = precision_score(y_true, y_pred, average="macro", zero_division=0)
    macro_rec = recall_score(y_true, y_pred, average="macro", zero_division=0)
    macro_f1 = f1_score(y_true, y_pred, average="macro", zero_division=0)

    target_names = [config.stage_labels[i] for i in range(1, config.num_classes + 1)]
    report_dict = classification_report(
        y_true,
        y_pred,
        labels=[0, 1, 2, 3, 4],
        target_names=target_names,
        digits=4,
        output_dict=True,
        zero_division=0
    )
    report_text = classification_report(
        y_true,
        y_pred,
        labels=[0, 1, 2, 3, 4],
        target_names=target_names,
        digits=4,
        zero_division=0
    )

    cm = confusion_matrix(y_true, y_pred, labels=[0, 1, 2, 3, 4])

    print("\n--- TEST SET OVERALL METRICS ---")
    print(f"Test Accuracy:        {acc:.4f}")
    print(f"Test Macro Precision: {macro_prec:.4f}")
    print(f"Test Macro Recall:    {macro_rec:.4f}")
    print(f"Test Macro F1-Score:  {macro_f1:.4f}")

    print("\n--- PER-CLASS CLASSIFICATION REPORT ---")
    print(report_text)

    print("\n--- CONFUSION MATRIX ---")
    print("Actual \\ Predicted")
    cm_df = pd.DataFrame(cm, index=[f"Actual {name}" for name in target_names], columns=[f"Pred Stage {i}" for i in range(1, 6)])
    print(cm_df)

    results = {
        "model_name": checkpoint.get("model_name", "mobilenet_v3_small"),
        "best_epoch": checkpoint.get("best_epoch", -1),
        "best_val_macro_f1": checkpoint.get("best_val_macro_f1", -1.0),
        "test_accuracy": float(acc),
        "test_macro_precision": float(macro_prec),
        "test_macro_recall": float(macro_rec),
        "test_macro_f1": float(macro_f1),
        "classification_report": report_dict,
        "confusion_matrix": cm.tolist()
    }

    results_path = config.model_dir / "test_evaluation_results.json"
    with open(results_path, "w") as f:
        json.dump(results, f, indent=2)

    print(f"\nSaved Test Results JSON to {results_path.resolve()}")
    print("=" * 60)

    return results
